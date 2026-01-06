package dev.mariany.copperworks.client.render;

import dev.mariany.copperworks.client.gl.CWRenderPipelines;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class CWRenderLayers {
    public static final RenderLayer.MultiPhase HIGHLIGHTED_BLOCK = RenderLayer.of(
            "highlighted_block",
            1536,
            false,
            true,
            CWRenderPipelines.HIGHLIGHTED_BLOCK,
            RenderLayer.MultiPhaseParameters
                    .builder()
                    .layering(RenderLayer.VIEW_OFFSET_Z_LAYERING)
                    .build(false)
    );
}
