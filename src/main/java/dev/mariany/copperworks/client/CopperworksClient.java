package dev.mariany.copperworks.client;

import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.client.gui.screen.ingame.InventoryNetworkScreen;
import dev.mariany.copperworks.client.item.RadioHandler;
import dev.mariany.copperworks.client.render.block.entity.BoundRelayBlockEntityRenderer;
import dev.mariany.copperworks.client.render.block.entity.RelayBlockEntityRenderer;
import dev.mariany.copperworks.client.render.item.property.bool.CWBooleanProperties;
import dev.mariany.copperworks.packet.clientbound.ClientboundPackets;
import dev.mariany.copperworks.screen.CWScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(EnvType.CLIENT)
public class CopperworksClient implements ClientModInitializer {
    private final RadioHandler radioHandler = new RadioHandler();

    @Override
    public void onInitializeClient() {
        ClientboundPackets.bootstrap();

        registerItemProperties();
        registerBlockRenderLayers();
        registerScreenHandlers();
        registerBlockEntityRenderers();

        ClientTickEvents.END_CLIENT_TICK.register(this.radioHandler::onTick);
    }

    private static void registerItemProperties() {
        CWBooleanProperties.bootstrap();
    }

    private static void registerBlockRenderLayers() {
        BlockRenderLayerMap.putBlock(CWBlocks.WOODEN_RAIL, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(CWBlocks.COPPER_RAIL, BlockRenderLayer.CUTOUT);
    }

    private static void registerScreenHandlers() {
        HandledScreens.register(CWScreenHandlers.INVENTORY_NETWORK, InventoryNetworkScreen::new);
    }

    private void registerBlockEntityRenderers() {
        BlockEntityRendererFactories.register(CWBlockEntities.BOUND_RELAY, BoundRelayBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(CWBlockEntities.RADIO_BOUND_RELAY, RelayBlockEntityRenderer::new);
    }
}
