package dev.mariany.copperworks.client.gl;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.mariany.copperworks.Copperworks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;

@Environment(EnvType.CLIENT)
public class CWRenderPipelines {
    public static final RenderPipeline HIGHLIGHTED_BLOCK = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                          .withLocation(Copperworks.id("pipeline/highlighted_block"))
                          .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP)
                          .withCull(true)
                          .withBlend(BlendFunction.TRANSLUCENT)
                          .withDepthWrite(false)
                          .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                          .build()
    );
}
