package dev.mariany.copperworks.packet.serverbound;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public record QuickMoveAllPacket(int syncId, ItemStack quickMoveStack) implements CustomPayload {
    public static final Id<QuickMoveAllPacket> ID = new Id<>(
            Copperworks.id("quick_move_all")
    );

    public static final PacketCodec<RegistryByteBuf, QuickMoveAllPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, QuickMoveAllPacket::syncId,
            ItemStack.PACKET_CODEC, QuickMoveAllPacket::quickMoveStack,
            QuickMoveAllPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void apply(QuickMoveAllPacket payload, ServerPlayNetworking.Context context) {
        float syncId = payload.syncId();
        ItemStack stack = payload.quickMoveStack();

        ServerPlayerEntity player = context.player();
        ScreenHandler handler = player.currentScreenHandler;

        player.updateLastActionTime();

        if (handler.syncId == syncId) {
            if (handler instanceof InventoryNetworkScreenHandler inventoryNetworkScreenHandler) {
                inventoryNetworkScreenHandler.quickMoveAll(stack);
            } else {
                Copperworks.LOGGER.warn(
                        "Player {} attempted to quick move all action inside invalid screen {}",
                        player,
                        handler
                );
            }
        }
    }
}
