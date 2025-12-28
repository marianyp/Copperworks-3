package dev.mariany.copperworks.packet.clientbound;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record InventoryScrollValidationPacket(int syncId, float scrollPosition) implements CustomPayload {
    public static final CustomPayload.Id<InventoryScrollValidationPacket> ID = new CustomPayload.Id<>(
            Copperworks.id("inventory_scroll_validation")
    );

    public static final PacketCodec<RegistryByteBuf, InventoryScrollValidationPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, InventoryScrollValidationPacket::syncId,
            PacketCodecs.FLOAT, InventoryScrollValidationPacket::scrollPosition,
            InventoryScrollValidationPacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
