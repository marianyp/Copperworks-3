package dev.mariany.copperworks.packet;

import dev.mariany.copperworks.packet.clientbound.StorageNetworkUpdate;
import dev.mariany.copperworks.packet.serverbound.StorageNetworkHandshake;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;

public class CWPackets {
    public static void bootstrap() {
        clientbound(PayloadTypeRegistry.playS2C());
        serverbound(PayloadTypeRegistry.playC2S());
    }

    private static void clientbound(PayloadTypeRegistry<RegistryByteBuf> registry) {
        registry.register(StorageNetworkUpdate.ID, StorageNetworkUpdate.CODEC);
    }

    private static void serverbound(PayloadTypeRegistry<RegistryByteBuf> registry) {
        registry.register(StorageNetworkHandshake.ID, StorageNetworkHandshake.CODEC);
    }
}
