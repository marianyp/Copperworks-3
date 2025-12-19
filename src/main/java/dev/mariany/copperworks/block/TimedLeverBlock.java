package dev.mariany.copperworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.block.OrientationHelper;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class TimedLeverBlock extends LeverBlock {
    protected final SoundEvent soundEvent;

    public TimedLeverBlock(SoundEvent soundEvent, Settings settings) {
        super(settings);

        this.soundEvent = soundEvent;

        this.setDefaultState(
                this.stateManager.getDefaultState()
                                 .with(FACING, Direction.NORTH)
                                 .with(POWERED, false)
                                 .with(FACE, BlockFace.WALL)
        );
    }

    protected int getPressTicks() {
        return 60;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(POWERED)) {
            world.setBlockState(pos, state.with(POWERED, false), Block.NOTIFY_ALL);
            this.updateNeighbors(state, world, pos);
            playClickSound(null, world, pos, false);
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (!moved && state.get(POWERED)) {
            this.updateNeighbors(state, world, pos);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (state.get(POWERED)) {
            return ActionResult.CONSUME;
        } else {
            this.powerOn(state, world, pos, player);
            return ActionResult.SUCCESS;
        }
    }

    public void powerOn(BlockState state, World world, BlockPos pos, @Nullable PlayerEntity player) {
        world.setBlockState(pos, state.with(POWERED, true), Block.NOTIFY_ALL);
        this.updateNeighbors(state, world, pos);
        world.scheduleBlockTick(pos, this, this.getPressTicks());
        playClickSound(player, world, pos, true);
        world.emitGameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
    }

    protected void updateNeighbors(BlockState state, World world, BlockPos pos) {
        Direction direction = getDirection(state).getOpposite();

        WireOrientation wireOrientation = OrientationHelper.getEmissionOrientation(
                world,
                direction,
                direction.getAxis().isHorizontal() ? Direction.UP : state.get(FACING)
        );

        world.updateNeighborsAlways(pos, this, wireOrientation);
        world.updateNeighborsAlways(pos.offset(direction), this, wireOrientation);
    }

    protected void playClickSound(
            @Nullable PlayerEntity player,
            WorldAccess world,
            BlockPos pos,
            boolean powered
    ) {
        float pitch = powered ? 0.6F : 0.5F;
        world.playSound(player, pos, this.soundEvent, SoundCategory.BLOCKS, 1, pitch);
    }

    @Override
    protected void onExploded(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            Explosion explosion,
            BiConsumer<ItemStack, BlockPos> stackMerger
    ) {
        if (explosion.canTriggerBlocks() && !state.get(POWERED)) {
            world.scheduleBlockTick(pos, this, this.getPressTicks());
        }

        super.onExploded(state, world, pos, explosion, stackMerger);
    }
}
