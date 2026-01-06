package dev.mariany.copperworks.packet.serverbound;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ServerboundPackets {
    public static void bootstrap() {
        ServerPlayNetworking.registerGlobalReceiver(InventoryNetworkHandshakePacket.ID, InventoryNetworkHandshakePacket::apply);
        ServerPlayNetworking.registerGlobalReceiver(InventoryScrollPacket.ID, InventoryScrollPacket::apply);
        ServerPlayNetworking.registerGlobalReceiver(UpdateSearchEntriesPacket.ID, UpdateSearchEntriesPacket::apply);
        ServerPlayNetworking.registerGlobalReceiver(UpdateSearchQueryPacket.ID, UpdateSearchQueryPacket::apply);
        ServerPlayNetworking.registerGlobalReceiver(QuickMoveAllPacket.ID, QuickMoveAllPacket::apply);
        ServerPlayNetworking.registerGlobalReceiver(RequestMufflersPacket.ID, RequestMufflersPacket::apply);
    }
}
