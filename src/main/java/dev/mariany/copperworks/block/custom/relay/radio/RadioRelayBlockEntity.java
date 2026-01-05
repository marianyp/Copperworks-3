package dev.mariany.copperworks.block.custom.relay.radio;

import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.custom.relay.HighlightedBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RadioRelayBlockEntity extends HighlightedBlockEntity {
    public RadioRelayBlockEntity(BlockPos pos, BlockState state) {
        this(CWBlockEntities.RADIO_RELAY, pos, state);
    }

    public RadioRelayBlockEntity(
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
