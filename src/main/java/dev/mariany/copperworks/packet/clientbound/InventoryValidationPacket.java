package dev.mariany.copperworks.packet.clientbound;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.Optional;

public record InventoryValidationPacket(int syncId, float scrollPosition, Optional<String> searchQuery)
        implements CustomPayload {
    public static final Id<InventoryValidationPacket> ID = new Id<>(
            Copperworks.id("inventory_validation")
    );

    public static final PacketCodec<RegistryByteBuf, InventoryValidationPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, InventoryValidationPacket::syncId,
            PacketCodecs.FLOAT, InventoryValidationPacket::scrollPosition,
            PacketCodecs.STRING.collect(PacketCodecs::optional), InventoryValidationPacket::searchQuery,

            InventoryValidationPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
