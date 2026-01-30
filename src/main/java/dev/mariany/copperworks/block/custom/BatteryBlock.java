package dev.mariany.copperworks.block.custom;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.block.custom.clock.ClockBlockEntity;
import dev.mariany.copperworks.tag.CWTags;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.OrientationHelper;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class BatteryBlock extends WallMountedBlock {
    public static final MapCodec<BatteryBlock> CODEC = createCodec(BatteryBlock::new);

    public static final BooleanProperty POWERED = Properties.POWERED;

    @Override
    protected MapCodec<? extends BatteryBlock> getCodec() {
        return CODEC;
    }

    public BatteryBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(
                this.getDefaultState()
                    .with(POWERED, false)
                    .with(FACING, Direction.NORTH)
                    .with(FACE, BlockFace.WALL)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING, FACE);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        boolean replacingAir = context.getWorld().getBlockState(context.getBlockPos()).isAir();

        if (!replacingAir && context.canReplaceExisting()) {
            return this.getDefaultState().with(FACE, BlockFace.FLOOR);
        }

        return super.getPlacementState(context);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return true;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return world.getBlockState(pos).get(POWERED) ? 15 : 0;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(POWERED) && !world.isReceivingRedstonePower(pos)) {
            powerOff(world, pos, state);
        }
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.getBlock() != state.getBlock() && world instanceof ServerWorld serverWorld) {
            update(serverWorld, pos, state);
        }
    }

    @Override
    protected void neighborUpdate(
            BlockState state,
            World world,
            BlockPos pos,
            Block sourceBlock,
            @Nullable WireOrientation wireOrientation,
            boolean notify
    ) {
        if (world instanceof ServerWorld serverWorld) {
            update(serverWorld, pos, state);
        }
    }

    protected static void update(ServerWorld world, BlockPos pos, BlockState state) {
        boolean receivingPower = world.isReceivingRedstonePower(pos);

        if (receivingPower != state.get(POWERED)) {
            if (receivingPower) {
                for (Direction axisDirection : getDirection(state).getAxis().getDirections()) {
                    sendPulse(world, pos, pos, axisDirection);
                }

                emitPulse(world, pos);
            } else {
                scheduleTick(world, pos);
            }
        }
    }

    protected static void powerOff(World world, BlockPos pos, BlockState state) {
        setPowered(world, pos, false);
        updateNeighbors(world, pos, state);
    }

    protected static void sendPulse(ServerWorld world, BlockPos pos, BlockPos origin, Direction direction) {
        BlockPos iterationPosition = pos.offset(direction, 1);

        while (true) {
            if (iterationPosition.equals(origin)) {
                return;
            }

            if (!world.isPosLoaded(iterationPosition)) {
                return;
            }

            boolean canInteract = !origin.offset(direction).equals(iterationPosition);
            BlockState state = world.getBlockState(iterationPosition);

            if (state.isIn(CWTags.Blocks.EXTENDS_BATTERY_PULSE)) {
                if (state.isIn(CWTags.Blocks.EXTENDS_BATTERY_PULSE_SAME_AXIS)) {
                    if (state.contains(Properties.FACING)) {
                        if (!state.get(Properties.FACING).getAxis().equals(direction.getAxis())) {
                            return;
                        }
                    }
                }
            } else {
                if (canInteract) {
                    handleInteraction(world, iterationPosition);
                }

                return;
            }

            iterationPosition = iterationPosition.offset(direction, 1);
        }
    }

    protected static void handleInteraction(World world, BlockPos pos) {
        if (!handleBattery(world, pos)) {
            handleClockBlockEntity(world, pos);
        }
    }

    protected static boolean handleBattery(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() instanceof BatteryBlock) {
            emitPulse(world, pos);
            return true;
        }

        return false;
    }

    protected static void handleClockBlockEntity(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ClockBlockEntity clockBlockEntity) {
            clockBlockEntity.resetProgress(world, pos, false);
            clockBlockEntity.playSound(world, pos);
        }
    }

    protected static void emitPulse(World world, BlockPos pos) {
        setPowered(world, pos, true);
        scheduleTick(world, pos);
    }

    protected static void scheduleTick(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();

        if (!world.isClient() && !world.getBlockTickScheduler().isQueued(pos, block)) {
            world.scheduleBlockTick(pos, block, 4);
        }
    }

    protected static void setPowered(World world, BlockPos pos, boolean powered) {
        BlockState state = world.getBlockState(pos).withIfExists(POWERED, powered);
        world.setBlockState(pos, state);
        updateNeighbors(world, pos, state);
    }

    protected static void updateNeighbors(World world, BlockPos pos, BlockState state) {
        Direction direction = getDirection(state);
        Block block = state.getBlock();

        for (Direction axisDirection : direction.getAxis().getDirections()) {
            BlockPos blockPos = pos.offset(axisDirection.getOpposite());
            WireOrientation wireOrientation = OrientationHelper.getEmissionOrientation(
                    world,
                    axisDirection.getOpposite(),
                    null
            );

            world.updateNeighbor(blockPos, block, wireOrientation);
            world.updateNeighborsExcept(blockPos, block, axisDirection, wireOrientation);
        }
    }
}
