package dev.mariany.copperworks.packet;

import dev.mariany.copperworks.packet.clientbound.InventoryNetworkUpdate;
import dev.mariany.copperworks.packet.serverbound.InventoryNetworkHandshake;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;

public class CWPackets {
    public static void bootstrap() {
        clientbound(PayloadTypeRegistry.playS2C());
        serverbound(PayloadTypeRegistry.playC2S());
    }

    private static void clientbound(PayloadTypeRegistry<RegistryByteBuf> registry) {
        registry.register(InventoryNetworkUpdate.ID, InventoryNetworkUpdate.CODEC);
    }

    private static void serverbound(PayloadTypeRegistry<RegistryByteBuf> registry) {
        registry.register(InventoryNetworkHandshake.ID, InventoryNetworkHandshake.CODEC);
    }
}
