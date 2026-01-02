package dev.mariany.copperworks.client.render;

import dev.mariany.copperworks.client.gl.CWRenderPipelines;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class CWRenderLayers {
    public static final RenderLayer.MultiPhase RELAY_HIGHLIGHT = RenderLayer.of(
            "relay_highlight",
            1536,
            false,
            true,
            CWRenderPipelines.RELAY_HIGHLIGHT,
            RenderLayer.MultiPhaseParameters
                    .builder()
                    .layering(RenderLayer.VIEW_OFFSET_Z_LAYERING)
                    .build(false)
    );
}
