package dev.mariany.copperworks.block.custom.clock;

import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ClockBlockEntity extends BlockEntity {
    protected final static int SECOND_IN_TICKS = 20;

    protected int target = ClockData.DEFAULT.target();
    protected int progress;
    protected int poweredTicks;

    public ClockBlockEntity(BlockPos pos, BlockState state) {
        this(CWBlockEntities.CLOCK, pos, state);
    }

    public ClockBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
    }

    public void interact(PlayerEntity player, BlockPos pos) {
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            int targetProgress = this.cycleTargetProgress(serverWorld, pos, player.isSneaking());
            int targetProgressSeconds = targetProgress == 0 ? 0 :
                    MathHelper.floor((float) targetProgress / SECOND_IN_TICKS);

            player.sendMessage(
                    Text.translatable("block.copperworks.clock.cycled", targetProgressSeconds),
                    true
            );

            playSound(serverWorld, pos, targetProgressSeconds);
        }
    }

    protected int cycleTargetProgress(World world, BlockPos pos, boolean shrink) {
        this.resetProgress(world, pos);

        int maxSeconds = this.getMaxSeconds();
        int target = this.target;

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

        this.target = target;

        return target;
    }

    protected int getMaxSeconds() {
        if (this.getCachedState().getBlock() instanceof AbstractClockBlock abstractClockBlock) {
            return abstractClockBlock.getMaxSeconds();
        }

        return 0;
    }

    public void playSound(World world, BlockPos pos) {
        playSound(world, pos, this.target);
    }

    protected static void playSound(World world, BlockPos pos, int targetProgressSeconds) {
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

    public void tick(World world, BlockPos pos, BlockState state) {
        int target = this.target;
        int poweredTicks = this.poweredTicks;
        int progress = this.progress;
        int nextProgress = progress + 1;

        if (--poweredTicks > 0) {
            this.poweredTicks = poweredTicks - 1;
            setPowered(world, pos, true);
        } else if (nextProgress >= target) {
            this.resetProgress(world, pos);
            setPowered(world, pos, true);
            this.poweredTicks = Math.max(0, this.getPoweredTicks());
        } else {
            if (state.get(AbstractClockBlock.POWERED, false)) {
                setPowered(world, pos, false);
            }

            this.progress = nextProgress;
        }
    }

    public void resetProgress(World world, BlockPos pos) {
        if (this.progress > 0) {
            this.progress = 0;
            update(world, pos);
        }
    }

    protected int getPoweredTicks() {
        if (this.getCachedState().getBlock() instanceof AbstractClockBlock abstractClockBlock) {
            return abstractClockBlock.getPoweredTicks();
        }

        return 0;
    }

    protected static void setPowered(World world, BlockPos pos, boolean powered) {
        world.setBlockState(pos, world.getBlockState(pos).withIfExists(AbstractClockBlock.POWERED, powered));
        update(world, pos);
    }

    protected static void update(World world, BlockPos pos) {
        world.updateNeighborsAlways(pos, world.getBlockState(pos).getBlock(), null);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);

        view.put(ClockData.KEY, ClockData.CODEC, new ClockData(this.target, this.progress));
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        ClockData state = view.read(ClockData.KEY, ClockData.CODEC).orElse(ClockData.DEFAULT);

        this.target = state.target();
        this.progress = state.progress();
    }
}
