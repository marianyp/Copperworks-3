package dev.mariany.copperworks.client.render.block.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class HighlightedBlockEntityRenderState extends BlockEntityRenderState {
    public Vec3d color;
    public float animationProgress;
}
