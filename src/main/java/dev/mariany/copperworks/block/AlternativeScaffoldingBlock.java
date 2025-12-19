package dev.mariany.copperworks.block;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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

public class AlternativeScaffoldingBlock extends Block implements Waterloggable, SelfInserting {
    private static final VoxelShape SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0, 0, 0.125, 0.125, 0.125, 0.875),
            VoxelShapes.cuboid(0, 0, 0.875, 1, 0.125, 1.0),
            VoxelShapes.cuboid(0, 0, 0, 1, 0.125, 0.125),
            VoxelShapes.cuboid(0.875, 0, 0.125, 1, 0.125, 0.875),
            VoxelShapes.cuboid(0, 0.875, 0, 1, 1, 1.0),
            VoxelShapes.cuboid(0.875, 0.125, 0, 1, 0.875, 0.125),
            VoxelShapes.cuboid(0, 0.125, 0.875, 0.125, 0.875, 1.0),
            VoxelShapes.cuboid(0, 0.125, 0, 0.125, 0.875, 0.125),
            VoxelShapes.cuboid(0.875, 0.125, 0.875, 1, 0.875, 1.0)
    );

    private static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public AlternativeScaffoldingBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState defaultPlacementState = super.getPlacementState(context);

        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();
        boolean waterlogged = world.getFluidState(blockPos).getFluid() == Fluids.WATER;

        if (defaultPlacementState == null) {
            return this.getDefaultState().with(WATERLOGGED, waterlogged);
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

        if (!world.isClient()) {
            tickView.scheduleBlockTick(pos, this, 1);
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

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState blockState) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.fullCube();
    }
}

