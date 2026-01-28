package dev.mariany.copperworks.block.custom.sensor;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSensorBlock extends WallMountedBlock implements BlockEntityProvider {
    public static final IntProperty POWER = Properties.POWER;

    public AbstractSensorBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    public static Direction getDirection(BlockState state) {
        return WallMountedBlock.getDirection(state);
    }

    @Override
    protected abstract MapCodec<? extends AbstractSensorBlock> getCodec();

    @Override
    @Nullable
    public abstract AbstractSensorBlockEntity createBlockEntity(BlockPos pos, BlockState state);

    protected abstract Stat<Identifier> getInteractStat();

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWER, FACE, FACING);
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWER);
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getDirection(state) == direction ? state.get(POWER) : 0;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof AbstractSensorBlockEntity abstractSensorBlockEntity) {
            player.incrementStat(this.getInteractStat());
            abstractSensorBlockEntity.interact(player, pos);
        }

        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return (tickWorld, pos, tickState, blockEntity) -> {
            if (tickWorld instanceof ServerWorld serverWorld &&
                    blockEntity instanceof AbstractSensorBlockEntity sensorBlockEntity) {
                sensorBlockEntity.tick(serverWorld, pos, tickState);
            }
        };
    }
}
