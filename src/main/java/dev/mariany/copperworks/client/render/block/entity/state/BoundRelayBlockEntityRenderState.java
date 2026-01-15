package dev.mariany.copperworks.client.render.block.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BoundRelayBlockEntityRenderState extends HighlightedBlockEntityRenderState {
    @Nullable
    public BlockPos boundPos;
}
