package dev.mariany.copperworks.packet;

import dev.mariany.copperworks.packet.clientbound.InventoryNetworkUpdatePacket;
import dev.mariany.copperworks.packet.clientbound.InventoryScrollValidationPacket;
import dev.mariany.copperworks.packet.serverbound.InventoryNetworkHandshakePacket;
import dev.mariany.copperworks.packet.serverbound.InventoryScrollPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;

public class CWPackets {
    public static void bootstrap() {
        clientbound(PayloadTypeRegistry.playS2C());
        serverbound(PayloadTypeRegistry.playC2S());
    }

    private static void clientbound(PayloadTypeRegistry<RegistryByteBuf> registry) {
        registry.register(InventoryNetworkUpdatePacket.ID, InventoryNetworkUpdatePacket.CODEC);
        registry.register(InventoryScrollValidationPacket.ID, InventoryScrollValidationPacket.CODEC);
    }

    private static void serverbound(PayloadTypeRegistry<RegistryByteBuf> registry) {
        registry.register(InventoryNetworkHandshakePacket.ID, InventoryNetworkHandshakePacket.CODEC);
        registry.register(InventoryScrollPacket.ID, InventoryScrollPacket.CODEC);
    }
}
