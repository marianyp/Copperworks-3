package dev.mariany.copperworks.client.render.block.entity;

import dev.mariany.copperworks.block.custom.relay.HighlightedBlockEntity;
import dev.mariany.copperworks.client.render.block.entity.state.HighlightedBlockEntityRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

@Environment(EnvType.CLIENT)
public class HighlightedBlockEntityRenderer
        extends AbstractHighlightedBlockEntityRenderer<HighlightedBlockEntity, HighlightedBlockEntityRenderState> {
    public HighlightedBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public HighlightedBlockEntityRenderState createRenderState() {
        return new HighlightedBlockEntityRenderState();
    }
}
