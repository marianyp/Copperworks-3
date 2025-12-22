package dev.mariany.copperworks.packet.serverbound;

import dev.mariany.copperworks.Copperworks;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;

public record StorageNetworkHandshake(GlobalPos connectionPos) implements CustomPayload {
    public static final CustomPayload.Id<StorageNetworkHandshake> ID = new CustomPayload.Id<>(
            Copperworks.id("storage_network_handshake")
    );

    public static final PacketCodec<ByteBuf, StorageNetworkHandshake> CODEC = PacketCodec.tuple(
            GlobalPos.PACKET_CODEC, StorageNetworkHandshake::connectionPos,
            StorageNetworkHandshake::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void apply(StorageNetworkHandshake payload, ServerPlayNetworking.Context context) {
        GlobalPos globalPos = payload.connectionPos();
        ServerPlayerEntity player = context.player();
        MinecraftServer server = player.getServer();

        if (server != null) {
            ServerWorld world = server.getWorld(globalPos.dimension());

            if (world != null) {
                BlockEntity blockEntity = world.getBlockEntity(globalPos.pos());

                if (blockEntity instanceof NamedScreenHandlerFactory namedScreenHandlerFactory) {
                    player.openHandledScreen(namedScreenHandlerFactory);
                }
            }
        }
    }
}
