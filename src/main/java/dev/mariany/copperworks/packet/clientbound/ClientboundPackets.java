package dev.mariany.copperworks.packet.clientbound;

import dev.mariany.copperworks.client.muffler.MufflerStorage;
import dev.mariany.copperworks.inventory.InventoryNetworkState;
import dev.mariany.copperworks.packet.serverbound.InventoryNetworkHandshakePacket;
import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import dev.mariany.copperworks.screen.scroll.ScrollableInventory;
import dev.mariany.copperworks.screen.search.SearchableInventory;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.GlobalPos;

import java.util.List;

public class ClientboundPackets {
    public static void bootstrap() {
        ClientPlayNetworking.registerGlobalReceiver(
                InventoryNetworkConnectionPacket.ID,
                (payload, context) -> {
                    ClientPlayerEntity player = context.player();
                    GlobalPos connectionPos = payload.connectionPos();

                    if (player instanceof InventoryNetworkState networkState) {
                        networkState.copperworks$setNetwork(payload.network());
                        context.responseSender().sendPacket(new InventoryNetworkHandshakePacket(connectionPos));
                    }
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                InventoryNetworkUpdatePacket.ID,
                (payload, context) -> {
                    int syncId = payload.syncId();
                    List<StackWithSlot> stacks = payload.stacks();
                    ClientPlayerEntity player = context.player();
                    ScreenHandler handler = player.currentScreenHandler;

                    if (handler instanceof InventoryNetworkScreenHandler inventoryNetworkScreenHandler) {
                        if (inventoryNetworkScreenHandler.syncId == syncId) {
                            inventoryNetworkScreenHandler.applyServerStacks(stacks);
                        }
                    }
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                InventoryValidationPacket.ID,
                (payload, context) -> {
                    float syncId = payload.syncId();
                    float scrollPosition = payload.scrollPosition();
                    String searchQuery = payload.searchQuery().orElse(null);

                    ClientPlayerEntity player = context.player();
                    ScreenHandler screenHandler = player.currentScreenHandler;

                    if (screenHandler.syncId == syncId && !player.isSpectator()) {
                        if (screenHandler instanceof ScrollableInventory scrollableInventory) {
                            scrollableInventory.onScrollValidation(scrollPosition);
                        }

                        if (screenHandler instanceof SearchableInventory searchableInventory) {
                            searchableInventory.onSearchQueryValidation(searchQuery);
                        }
                    }
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                MuffledAreasPacket.ID,
                (payload, context) -> MufflerStorage.put(
                        payload.chunkPos(),
                        payload.muffledAreas()
                )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                MuffledAreaUpdatedPacket.ID,
                (payload, context) -> MufflerStorage.update(
                        payload.chunkPos(),
                        payload.muffledArea()
                )
        );
    }
}
