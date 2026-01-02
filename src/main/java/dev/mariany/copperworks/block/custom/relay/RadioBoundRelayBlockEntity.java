package dev.mariany.copperworks.block.custom.relay;

import dev.mariany.copperworks.block.CWBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RadioBoundRelayBlockEntity extends AbstractRelayBlockEntity {
    public RadioBoundRelayBlockEntity(BlockPos pos, BlockState state) {
        this(CWBlockEntities.RADIO_BOUND_RELAY, pos, state);
    }

    public RadioBoundRelayBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state, new Vec3d(1, 0, 0));
    }

    @Override
    public void focus(boolean focus) {
        this.animator.setFocused(focus);
    }
}
