package dev.mariany.copperworks.block.custom.sensor;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.stat.CWStats;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.Map;
import java.util.function.Function;

public class SensorBlock extends AbstractSensorBlock {
    public static final MapCodec<SensorBlock> CODEC = SensorBlock.createCodec(SensorBlock::new);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private final Function<BlockState, VoxelShape> shapeFunction;

    public SensorBlock(Settings settings) {
        super(settings);
        this.shapeFunction = this.createShapeFunction();
    }

    protected Function<BlockState, VoxelShape> createShapeFunction() {
        Map<BlockFace, Map<Direction, VoxelShape>> map = VoxelShapes.createBlockFaceHorizontalFacingShapeMap(
                Block.createCuboidZShape(12, 8, 12, 16)
        );

        return this.createShapeFunction(state -> map.get(state.get(FACE)).get(state.get(FACING)));
    }

    @Override
    protected MapCodec<? extends SensorBlock> getCodec() {
        return CODEC;
    }

    @Override
    public SensorBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SensorBlockEntity(pos, state);
    }

    @Override
    protected Stat<Identifier> getInteractStat() {
        return Stats.CUSTOM.getOrCreateStat(CWStats.INTERACT_WITH_SENSOR);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFunction.apply(state);
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
}
