package dev.mariany.copperworks.client.render.block.entity;

import dev.mariany.copperworks.block.custom.relay.HighlightedBlockEntity;
import dev.mariany.copperworks.client.render.CWRenderLayers;
import dev.mariany.copperworks.client.render.block.entity.state.HighlightedBlockEntityRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class AbstractHighlightedBlockEntityRenderer<
        T extends HighlightedBlockEntity,
        S extends HighlightedBlockEntityRenderState
        > implements BlockEntityRenderer<T, S> {
    protected final float maxOpacity;

    public AbstractHighlightedBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this(context, 0.5F);
    }

    public AbstractHighlightedBlockEntityRenderer(BlockEntityRendererFactory.Context context, float maxOpacity) {
        this.maxOpacity = maxOpacity;
    }

    @Override
    public int getRenderDistance() {
        return 256;
    }

    @Override
    public void updateRenderState(
            T blockEntity,
            S state,
            float tickProgress,
            Vec3d cameraPos,
            @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.color = blockEntity.getColor();
        state.animationProgress = blockEntity.getAnimationProgress(tickProgress);
    }

    @Override
    public void render(
            S state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState
    ) {
        renderHighlight(state, matrices, queue, cameraState, state.pos);
    }

    protected void renderHighlight(
            HighlightedBlockEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState,
            BlockPos pos
    ) {
        float progress = state.animationProgress;

        if (progress > 0) {
            Box box = Box.from(Vec3d.of(pos)).offset(cameraState.pos.negate());

            renderHighlight(queue, matrices, state.color, box, progress);
        }
    }

    protected void renderHighlight(
            OrderedRenderCommandQueue queue,
            MatrixStack matrices,
            Vec3d color,
            Box box,
            float progress
    ) {
        queue.submitCustom(
                matrices,
                CWRenderLayers.HIGHLIGHTED_BLOCK,
                (matricesEntry, vertexConsumer) -> VoxelShapes
                        .fullCube()
                        .forEachBox(
                                (minX, minY, minZ, maxX, maxY, maxZ) ->
                                        VertexRendering.drawFilledBox(
                                                matrices,
                                                vertexConsumer,
                                                box.minX,
                                                box.minY,
                                                box.minZ,
                                                box.maxX,
                                                box.maxY,
                                                box.maxZ,
                                                (float) color.getX(),
                                                (float) color.getY(),
                                                (float) color.getZ(),
                                                progress * this.maxOpacity
                                        )
                        )
        );
    }
}
