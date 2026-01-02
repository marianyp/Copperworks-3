package dev.mariany.copperworks.client.render.block.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RelayBlockEntityRenderState extends BlockEntityRenderState {
    public Vec3d color;
    public float animationProgress;
    @Nullable
    public BlockPos boundPos;
}
