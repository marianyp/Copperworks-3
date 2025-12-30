package dev.mariany.copperworks.packet.serverbound;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.screen.scroll.ScrollableInventory;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public record InventoryScrollPacket(int syncId, float scrollPosition) implements CustomPayload {
    public static final Id<InventoryScrollPacket> ID = new Id<>(
            Copperworks.id("inventory_scroll")
    );

    public static final PacketCodec<ByteBuf, InventoryScrollPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, InventoryScrollPacket::syncId,
            PacketCodecs.FLOAT, InventoryScrollPacket::scrollPosition,
            InventoryScrollPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void apply(InventoryScrollPacket payload, ServerPlayNetworking.Context context) {
        float syncId = payload.syncId();
        float scrolledPosition = payload.scrollPosition();

        ServerPlayerEntity player = context.player();
        ScreenHandler screenHandler = player.currentScreenHandler;

        player.updateLastActionTime();

        if (screenHandler.syncId == syncId && !player.isSpectator()) {
            if (screenHandler instanceof ScrollableInventory scrollableInventory && screenHandler.canUse(player)) {
                scrollableInventory.onScroll(scrolledPosition);
                screenHandler.sendContentUpdates();
            } else {
                Copperworks.LOGGER.debug("Player {} interacted with invalid menu {}", player, screenHandler);
            }
        }
    }
}
