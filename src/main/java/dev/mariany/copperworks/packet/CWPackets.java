package dev.mariany.copperworks.packet;

import dev.mariany.copperworks.packet.clientbound.InventoryNetworkUpdatePacket;
import dev.mariany.copperworks.packet.clientbound.InventoryValidationPacket;
import dev.mariany.copperworks.packet.serverbound.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;

public class CWPackets {
    public static void bootstrap() {
        clientbound(PayloadTypeRegistry.playS2C());
        serverbound(PayloadTypeRegistry.playC2S());
    }

    private static void clientbound(PayloadTypeRegistry<RegistryByteBuf> registry) {
        registry.register(InventoryNetworkUpdatePacket.ID, InventoryNetworkUpdatePacket.CODEC);
        registry.register(InventoryValidationPacket.ID, InventoryValidationPacket.CODEC);
    }

    private static void serverbound(PayloadTypeRegistry<RegistryByteBuf> registry) {
        registry.register(InventoryNetworkHandshakePacket.ID, InventoryNetworkHandshakePacket.CODEC);
        registry.register(InventoryScrollPacket.ID, InventoryScrollPacket.CODEC);
        registry.register(UpdateSearchEntriesPacket.ID, UpdateSearchEntriesPacket.CODEC);
        registry.register(UpdateSearchQueryPacket.ID, UpdateSearchQueryPacket.CODEC);
        registry.register(QuickMoveAllPacket.ID, QuickMoveAllPacket.CODEC);
    }
}
