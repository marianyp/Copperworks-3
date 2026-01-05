package dev.mariany.copperworks.block.custom.sensor;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public class SensorBlock extends WallMountedBlock implements BlockEntityProvider {
    public static final MapCodec<SensorBlock> CODEC = SensorBlock.createCodec(SensorBlock::new);

    public static final IntProperty POWER = Properties.POWER;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private final Function<BlockState, VoxelShape> shapeFunction;

    public SensorBlock(AbstractBlock.Settings settings) {
        super(settings);

        this.shapeFunction = this.createShapeFunction();
    }

    protected Function<BlockState, VoxelShape> createShapeFunction() {
        Map<BlockFace, Map<Direction, VoxelShape>> map = VoxelShapes.createBlockFaceHorizontalFacingShapeMap(
                Block.createCuboidZShape(12, 8, 12, 16)
        );

        return this.createShapeFunction(state -> map.get(state.get(FACE)).get(state.get(FACING)));
    }

    public static Direction getDirection(BlockState state) {
        return WallMountedBlock.getDirection(state);
    }

    @Override
    protected MapCodec<? extends SensorBlock> getCodec() {
        return CODEC;
    }

    @Override
    @Nullable
    public SensorBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SensorBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWER, WATERLOGGED, FACE, FACING);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFunction.apply(state);
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
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();
        World world = ctx.getWorld();
        BlockState defaultPlacementState = super.getPlacementState(ctx);
        boolean waterlogged = world.getFluidState(blockPos).getFluid() == Fluids.WATER;

        if (defaultPlacementState == null) {
            return null;
        }

        return defaultPlacementState.with(WATERLOGGED, waterlogged);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            WorldView world,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random
    ) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(
                state,
                world,
                tickView,
                pos,
                direction,
                neighborPos,
                neighborState,
                random
        );
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
                    blockEntity instanceof SensorBlockEntity sensorBlockEntity) {
                sensorBlockEntity.tick(serverWorld, pos, tickState);
            }
        };
    }
}
