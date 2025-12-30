package dev.mariany.copperworks.block.entity.clock;

import dev.mariany.copperworks.block.entity.CWBlockEntities;
import dev.mariany.copperworks.stat.CWStats;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CopperClockBlockEntity extends BlockEntity implements ClockBlockEntity {
    protected int target = ClockBlockEntity.SECOND_IN_TICKS;
    protected int progress = 0;
    protected int poweredTicks = 0;

    public CopperClockBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(CWBlockEntities.COPPER_CLOCK, blockPos, blockState);
    }

    public CopperClockBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, CopperClockBlockEntity clockBlockEntity) {
        ClockBlockEntity.tick(clockBlockEntity, world, pos, state);
    }

    public void interact(PlayerEntity player, BlockPos pos) {
        player.incrementStat(CWStats.INTERACT_WITH_COPPER_CLOCK);
        ClockBlockEntity.super.interact(player, pos);
    }

    @Override
    public int getMaxSeconds() {
        return 10;
    }

    @Override
    public int getPoweredDuration() {
        return 2;
    }

    @Override
    public int getTarget() {
        return this.target;
    }

    @Override
    public void setTarget(int target) {
        this.target = target;
    }

    @Override
    public int getProgress() {
        return this.progress;
    }

    @Override
    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public int getPoweredTicks() {
        return this.poweredTicks;
    }

    @Override
    public void setPoweredTicks(int poweredTicks) {
        this.poweredTicks = poweredTicks;
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        ClockBlockEntity.writeData(view, this);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        ClockBlockEntity.readData(view, this);
    }
}
