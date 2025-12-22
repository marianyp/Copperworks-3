package dev.mariany.copperworks.packet.clientbound;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.inventory.StorageNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.GlobalPos;

public record StorageNetworkUpdate(GlobalPos connectionPos, StorageNetwork network) implements CustomPayload {
    public static final CustomPayload.Id<StorageNetworkUpdate> ID = new CustomPayload.Id<>(
            Copperworks.id("storage_network_update")
    );

    public static final PacketCodec<RegistryByteBuf, StorageNetworkUpdate> CODEC = PacketCodec.tuple(
            GlobalPos.PACKET_CODEC, StorageNetworkUpdate::connectionPos,
            StorageNetwork.PACKET_CODEC, StorageNetworkUpdate::network,
            StorageNetworkUpdate::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
