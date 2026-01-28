package dev.mariany.copperworks.client.render;

import dev.mariany.copperworks.client.gl.CWRenderPipelines;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.LayeringTransform;
import net.minecraft.client.render.OutputTarget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;

@Environment(EnvType.CLIENT)
public final class CWRenderLayers {
    public static final RenderLayer HIGHLIGHTED_BLOCK = RenderLayer.of(
            "highlighted_block",
            RenderSetup.builder(CWRenderPipelines.HIGHLIGHTED_BLOCK)
                    .expectedBufferSize(1536)
                    .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .outputTarget(OutputTarget.OUTLINE_TARGET)
                    .build()
    );

    private CWRenderLayers() {
    }
}
