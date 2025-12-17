package dev.mariany.copperworks.client;

import dev.mariany.copperworks.block.CWBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.render.BlockRenderLayer;

public class CopperworksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerBlockRenderLayers();
    }

    private void registerBlockRenderLayers() {
        BlockRenderLayerMap.putBlock(CWBlocks.WOODEN_RAIL, BlockRenderLayer.CUTOUT);
    }
}
