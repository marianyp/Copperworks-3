package dev.mariany.copperworks.packet.clientbound;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.inventory.InventoryNetwork;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;

public record InventoryNetworkUpdatePacket(int syncId, List<StackWithSlot> stacks) implements CustomPayload {
    public static final Id<InventoryNetworkUpdatePacket> ID = new Id<>(
            Copperworks.id("inventory_network_update")
    );

    public static final PacketCodec<RegistryByteBuf, InventoryNetworkUpdatePacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            InventoryNetworkUpdatePacket::syncId,
            InventoryNetwork.STACK_WITH_SLOT_PACKET_CODEC.collect(PacketCodecs.toList()),
            InventoryNetworkUpdatePacket::stacks,
            InventoryNetworkUpdatePacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
