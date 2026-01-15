package dev.mariany.copperworks.client.render.block.entity;

import dev.mariany.copperworks.block.custom.relay.bound.BoundRelayBlockEntity;
import dev.mariany.copperworks.client.render.block.entity.state.BoundRelayBlockEntityRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class BoundRelayBlockEntityRenderer
        extends AbstractHighlightedBlockEntityRenderer<BoundRelayBlockEntity, BoundRelayBlockEntityRenderState> {
    public BoundRelayBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    protected static Optional<BlockPos> getBoundPosition(BoundRelayBlockEntity boundRelayBlockEntity) {
        return boundRelayBlockEntity.getBoundPos().map(boundPos -> {
            RegistryKey<World> dimension = boundPos.dimension();
            ClientWorld world = MinecraftClient.getInstance().world;

            if (world != null) {
                if (dimension.equals(world.getRegistryKey())) {
                    return boundPos.pos();
                }
            }

            return null;
        });
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    public boolean isInRenderDistance(BoundRelayBlockEntity boundRelayBlockEntity, Vec3d cameraPos) {
        return getBoundPosition(boundRelayBlockEntity)
                .map(pos -> isInRenderDistance(pos, cameraPos))
                .orElse(super.isInRenderDistance(boundRelayBlockEntity, cameraPos));
    }

    public boolean isInRenderDistance(BlockPos pos, Vec3d cameraPos) {
        return Vec3d.ofCenter(pos).isInRange(cameraPos, this.getRenderDistance());
    }

    @Override
    public BoundRelayBlockEntityRenderState createRenderState() {
        return new BoundRelayBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(
            BoundRelayBlockEntity boundRelayBlockEntity,
            BoundRelayBlockEntityRenderState state,
            float tickProgress,
            Vec3d cameraPos,
            @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(boundRelayBlockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.boundPos = getBoundPosition(boundRelayBlockEntity).orElse(null);
    }

    @Override
    public void render(
            BoundRelayBlockEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState
    ) {
        super.render(state, matrices, queue, cameraState);

        BlockPos boundPos = state.boundPos;

        if (boundPos != null) {
            renderHighlight(state, matrices, queue, cameraState, boundPos);
        }
    }
}
