package dev.mariany.copperworks.block.custom.clock;

import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public interface ClockBlockEntity {
    BooleanProperty POWERED = Properties.POWERED;
    int SECOND_IN_TICKS = 20;

    int getMaxSeconds();

    int getPoweredDuration();

    int getTarget();

    void setTarget(int target);

    int getProgress();

    void setProgress(int progress);

    int getPoweredTicks();

    void setPoweredTicks(int poweredTicks);

    static void tick(ClockBlockEntity clockBlockEntity, World world, BlockPos pos, BlockState state) {
        int target = clockBlockEntity.getTarget();
        int poweredTicks = clockBlockEntity.getPoweredTicks();
        int progress = clockBlockEntity.getProgress();
        int nextProgress = progress + 1;

        if (--poweredTicks > 0) {
            clockBlockEntity.setPoweredTicks(poweredTicks - 1);
            setPowered(world, pos, true);
        } else if (nextProgress >= target) {
            clockBlockEntity.resetProgress(world, pos);
            setPowered(world, pos, true);
            clockBlockEntity.setPoweredTicks(Math.max(0, clockBlockEntity.getPoweredDuration()));
        } else {
            if (state.get(POWERED, false)) {
                setPowered(world, pos, false);
            }

            clockBlockEntity.setProgress(nextProgress);
        }
    }

    private static void interactInternal(ClockBlockEntity clockBlockEntity, PlayerEntity player, BlockPos pos) {
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            int targetProgress = clockBlockEntity.cycleTargetProgress(serverWorld, pos, player.isSneaking());
            int targetProgressSeconds = targetProgress == 0 ? 0 : MathHelper.floor((float) targetProgress / 20);

            player.sendMessage(
                    Text.translatable("block.copperworks.clock.cycled", targetProgressSeconds),
                    true
            );

            clockBlockEntity.playSound(serverWorld, pos, targetProgressSeconds);
        }
    }

    default void playSound(World world, BlockPos pos) {
        this.playSound(world, pos, this.getTarget());
    }

    default void playSound(World world, BlockPos pos, int targetProgressSeconds) {
        world.playSound(
                null,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                CWSoundEvents.BLOCK_CLOCK_INTERACT,
                SoundCategory.NEUTRAL,
                0.5F,
                (float) (1.6 - (Math.min(12, targetProgressSeconds) - 1) * 0.1)
        );
    }

    default void interact(PlayerEntity player, BlockPos pos) {
        interactInternal(this, player, pos);
    }

    default int cycleTargetProgress(World world, BlockPos pos, boolean shrink) {
        this.resetProgress(world, pos);

        int maxSeconds = this.getMaxSeconds();
        int target = this.getTarget();

        if (shrink) {
            target = target - SECOND_IN_TICKS;

            if (target <= 0) {
                target = maxSeconds * SECOND_IN_TICKS; // Wrap to maximum if below minimum
            }
        } else {
            target = target + SECOND_IN_TICKS;

            if (target > maxSeconds * SECOND_IN_TICKS) {
                target = SECOND_IN_TICKS; // Wrap to minimum if above maximum
            }
        }

        this.setTarget(target);

        return target;
    }

    default void resetProgress(World world, BlockPos pos) {
        if (this.getProgress() > 0) {
            this.setProgress(0);
            update(world, pos);
        }
    }

    static void update(World world, BlockPos pos) {
        world.updateNeighborsAlways(pos, world.getBlockState(pos).getBlock(), null);
    }

    private static void setPowered(World world, BlockPos pos, boolean powered) {
        world.setBlockState(pos, world.getBlockState(pos).withIfExists(POWERED, powered));
        update(world, pos);
    }

    static void writeData(WriteView view, ClockBlockEntity clockBlockEntity) {
        view.put(
                ClockState.KEY,
                ClockState.CODEC,
                new ClockState(clockBlockEntity.getTarget(), clockBlockEntity.getProgress())
        );
    }

    static void readData(ReadView view, ClockBlockEntity clockBlockEntity) {
        ClockState state = view.read(ClockState.KEY, ClockState.CODEC).orElse(ClockState.DEFAULT);
        clockBlockEntity.setTarget(state.target());
        clockBlockEntity.setProgress(state.progress());
    }
}
