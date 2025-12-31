package dev.mariany.copperworks.block.clock;

import dev.mariany.copperworks.block.entity.clock.ClockBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public abstract class AbstractClockBlock extends BlockWithEntity {
    public AbstractClockBlock(Settings settings) {
        super(settings);

        this.setDefaultState(
                this.stateManager.getDefaultState()
                                 .with(ClockBlockEntity.POWERED, false)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ClockBlockEntity.POWERED);
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.getWeakRedstonePower(world, pos, direction);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(ClockBlockEntity.POWERED) ? 15 : 0;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof ClockBlockEntity clockBlockEntity) {
            clockBlockEntity.interact(player, pos);
        }

        return ActionResult.SUCCESS;
    }
}
