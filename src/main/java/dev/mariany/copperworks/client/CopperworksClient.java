package dev.mariany.copperworks.client;

import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.client.gui.screen.ingame.InventoryNetworkScreen;
import dev.mariany.copperworks.packet.clientbound.ClientboundPackets;
import dev.mariany.copperworks.screen.CWScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;

@Environment(EnvType.CLIENT)
public class CopperworksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientboundPackets.bootstrap();

        registerBlockRenderLayers();
        registerScreenHandlers();
    }

    private static void registerBlockRenderLayers() {
        BlockRenderLayerMap.putBlock(CWBlocks.WOODEN_RAIL, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(CWBlocks.COPPER_RAIL, BlockRenderLayer.CUTOUT);
    }

    private static void registerScreenHandlers() {
        HandledScreens.register(CWScreenHandlers.INVENTORY_NETWORK, InventoryNetworkScreen::new);
    }
}
