package dev.mariany.copperworks.block.custom.relay.ender;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.server.world.CWChunkTickets;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.List;

public interface EnderRelayTracker {
    String ROOT_KEY = Copperworks.id("ender_relays").toString();
    String KEY = "ender_relay";

    List<GlobalPos> copperworks$getEnderRelays();

    void copperworks$addEnderRelay(GlobalPos globalPos);

    void copperworks$removeEnderRelay(GlobalPos globalPos);

    long copperworks$handleEnderRelay(GlobalPos globalPos);

    static void writeEnderRelays(WriteView view, EnderRelayTracker enderRelayTracker, Entity entity) {
        WriteView.ListView listView = view.getList(ROOT_KEY);
        List<GlobalPos> enderRelays = enderRelayTracker.copperworks$getEnderRelays();

        if (entity.getEntityWorld() instanceof ServerWorld world) {
            for (GlobalPos enderRelayPos : enderRelays) {
                RegistryKey<World> dimension = enderRelayPos.dimension();
                BlockPos pos = enderRelayPos.pos();

                ServerWorld enderRelayWorld = world.getServer().getWorld(dimension);

                if (enderRelayWorld == null) {
                    Copperworks.LOGGER.warn(
                            "Trying to save ender relay without level ({}) being loaded, skipping",
                            dimension
                    );
                } else {
                    if (enderRelayWorld.getBlockEntity(pos) instanceof EnderRelayBlockEntity enderRelayBlockEntity) {
                        if (enderRelayBlockEntity.isOwner(entity)) {
                            WriteView writeView = listView.add();
                            writeView.put(KEY, GlobalPos.CODEC, enderRelayPos);
                        } else {
                            Copperworks.LOGGER.warn(
                                    "Trying to save unowned ender relay at ({}), skipping",
                                    enderRelayPos
                            );
                        }
                    } else {
                        Copperworks.LOGGER.warn("Trying to save removed ender relay at ({}), skipping", enderRelayPos);
                    }
                }
            }
        }
    }

    static void readEnderRelays(ReadView view, Entity entity) {
        view.getListReadView(ROOT_KEY).forEach(readView -> readEnderRelay(readView, entity));
    }

    private static void readEnderRelay(ReadView view, Entity entity) {
        view.read(KEY, GlobalPos.CODEC).ifPresent(enderRelayPos -> {
            if (entity.getEntityWorld() instanceof ServerWorld world) {
                RegistryKey<World> dimension = enderRelayPos.dimension();
                BlockPos pos = enderRelayPos.pos();

                ServerWorld enderRelayWorld = world.getServer().getWorld(dimension);

                if (enderRelayWorld == null) {
                    Copperworks.LOGGER.warn(
                            "Trying to load ender relay without level ({}) being loaded, skipping",
                            dimension
                    );
                } else {
                    if (enderRelayWorld.getBlockEntity(pos) instanceof EnderRelayBlockEntity enderRelayBlockEntity) {
                        if (enderRelayBlockEntity.isOwner(entity)) {
                            addEnderRelayTicket(enderRelayWorld, entity.getChunkPos());
                            enderRelayBlockEntity.tick(enderRelayWorld);
                        } else {
                            Copperworks.LOGGER.warn(
                                    "Skipping ender relay at ({}), owner has been changed",
                                    enderRelayPos
                            );
                        }
                    } else {
                        Copperworks.LOGGER.warn("Failed to locate ender relay at ({}), skipping", enderRelayPos);
                    }
                }
            }
        });
    }

    static long addEnderRelayTicket(ServerWorld world, ChunkPos chunkPos) {
        world.getChunkManager().addTicket(CWChunkTickets.ENDER_RELAY, chunkPos, 2);
        return CWChunkTickets.ENDER_RELAY.expiryTicks();
    }
}
