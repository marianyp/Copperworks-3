package dev.mariany.copperworks.packet.clientbound;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.inventory.InventoryNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.GlobalPos;

public record InventoryNetworkConnectionPacket(GlobalPos connectionPos, InventoryNetwork network) implements CustomPayload {
    public static final CustomPayload.Id<InventoryNetworkConnectionPacket> ID = new CustomPayload.Id<>(
            Copperworks.id("inventory_network_connection")
    );

    public static final PacketCodec<RegistryByteBuf, InventoryNetworkConnectionPacket> CODEC = PacketCodec.tuple(
            GlobalPos.PACKET_CODEC, InventoryNetworkConnectionPacket::connectionPos,
            InventoryNetwork.PACKET_CODEC, InventoryNetworkConnectionPacket::network,
            InventoryNetworkConnectionPacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
