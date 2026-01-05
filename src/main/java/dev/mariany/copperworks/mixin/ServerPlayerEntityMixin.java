package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.block.custom.relay.ender.EnderRelayTracker;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements EnderRelayTracker {
    @Unique
    private final Set<GlobalPos> enderRelays = new HashSet<>();

    @Override
    public List<GlobalPos> copperworks$getEnderRelays() {
        return this.enderRelays.stream().toList();
    }

    @Override
    public void copperworks$addEnderRelay(GlobalPos globalPos) {
        this.enderRelays.add(globalPos);
    }

    @Override
    public void copperworks$removeEnderRelay(GlobalPos globalPos) {
        this.enderRelays.remove(globalPos);
    }

    @Override
    public long copperworks$handleEnderRelay(GlobalPos globalPos) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        RegistryKey<World> dimension = globalPos.dimension();
        BlockPos pos = globalPos.pos();

        ServerWorld world = player.getEntityWorld();

        MinecraftServer server = world.getServer();

        ServerWorld enderRelayWorld = server.getWorld(dimension);

        if (enderRelayWorld != null) {
            this.copperworks$addEnderRelay(globalPos);
            enderRelayWorld.resetIdleTimeout();
            return EnderRelayTracker.addEnderRelayTicket(enderRelayWorld, new ChunkPos(pos)) - 1;
        }

        return 0;
    }

    @Inject(method = "writeCustomData", at = @At(value = "HEAD"))
    protected void injectWriteCustomData(WriteView view, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (player instanceof EnderRelayTracker enderRelayTracker) {
            EnderRelayTracker.writeEnderRelays(view, enderRelayTracker, player);
        }
    }
}
