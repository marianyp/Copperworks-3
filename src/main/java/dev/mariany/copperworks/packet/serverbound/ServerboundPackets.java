package dev.mariany.copperworks.packet.serverbound;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ServerboundPackets {
    public static void bootstrap() {
        ServerPlayNetworking.registerGlobalReceiver(InventoryNetworkHandshake.ID, InventoryNetworkHandshake::apply);
    }
}
