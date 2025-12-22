package dev.mariany.copperworks.packet.clientbound;

import dev.mariany.copperworks.inventory.NetworkState;
import dev.mariany.copperworks.packet.serverbound.StorageNetworkHandshake;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.GlobalPos;

public class ClientboundPackets {
    public static void bootstrap() {
        ClientPlayNetworking.registerGlobalReceiver(
                StorageNetworkUpdate.ID,
                (payload, context) -> {
                    ClientPlayerEntity player = context.player();
                    GlobalPos connectionPos = payload.connectionPos();

                    if(player instanceof NetworkState networkState) {
                        networkState.copperworks2$setNetwork(payload.network());
                        context.responseSender().sendPacket(new StorageNetworkHandshake(connectionPos));
                    }
                }
        );
    }
}
