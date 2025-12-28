package dev.mariany.copperworks.packet.clientbound;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.inventory.InventoryNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.GlobalPos;

public record InventoryNetworkUpdatePacket(GlobalPos connectionPos, InventoryNetwork network) implements CustomPayload {
    public static final CustomPayload.Id<InventoryNetworkUpdatePacket> ID = new CustomPayload.Id<>(
            Copperworks.id("inventory_network_update")
    );

    public static final PacketCodec<RegistryByteBuf, InventoryNetworkUpdatePacket> CODEC = PacketCodec.tuple(
            GlobalPos.PACKET_CODEC, InventoryNetworkUpdatePacket::connectionPos,
            InventoryNetwork.PACKET_CODEC, InventoryNetworkUpdatePacket::network,
            InventoryNetworkUpdatePacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
