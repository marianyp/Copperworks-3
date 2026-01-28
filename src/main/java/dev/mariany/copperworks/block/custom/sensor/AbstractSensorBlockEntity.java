package dev.mariany.copperworks.block.custom.sensor;

import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.List;

public abstract class AbstractSensorBlockEntity extends BlockEntity {
    protected final int maxRange;
    protected int range;

    public AbstractSensorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxRange) {
        super(type, pos, state);

        if (maxRange <= 0) {
            throw new IllegalStateException("maxRange must be > 0");
        }

        this.maxRange = maxRange;
        this.range = maxRange;
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        if (this.world != null) {
            updateNeighbors(this.world, pos, oldState);
        }
    }

    public void interact(PlayerEntity player, BlockPos pos) {
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            this.cycleRange(!player.isSneaking());

            player.sendMessage(Text.translatable("block.copperworks.sensor.cycled", this.range), true);

            serverWorld.playSound(
                    null,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    CWSoundEvents.BLOCK_SENSOR_INTERACT,
                    SoundCategory.BLOCKS,
                    0.5F,
                    (float) (1.6 - (this.range - 1) * 0.1)
            );
        }
    }

    protected void cycleRange(boolean increment) {
        if (increment) {
            if (++this.range > this.maxRange) {
                this.range = 1;
            }
        } else {
            if (--this.range <= 0) {
                this.range = this.maxRange;
            }
        }
    }

    public void tick(ServerWorld world, BlockPos pos, BlockState state) {
        Direction facing = AbstractSensorBlock.getDirection(state);

        Vec3d start = pos.toCenterPos();

        int visibleBlocks = 0;

        for (; visibleBlocks < this.range; visibleBlocks++) {
            BlockPos hitPos = pos.offset(facing, visibleBlocks + 1);
            BlockState hitState = world.getBlockState(hitPos);

            if (hitState.isOpaque() && stateBlocksVision(world, hitPos, hitState, facing)) {
                break;
            }
        }

        Vec3d end = start.offset(facing, visibleBlocks);

        Box visibleArea = Box.enclosing(pos, BlockPos.ofFloored(end));

        List<Entity> visibleEntities = world.getEntitiesByClass(
                Entity.class,
                visibleArea,
                entity -> !entity.isSpectator() && !entity.isInvisible() && entity.isLiving()
        );

        int power = Math.clamp(visibleEntities.size(), 0, 15);

        if (state.get(AbstractSensorBlock.POWER, 0) != power) {
            updatePower(world, pos, state, power);
        }
    }

    protected static boolean stateBlocksVision(World world, BlockPos pos, BlockState state, Direction direction) {
        return state.isSideSolidFullSquare(world, pos, direction) || state.isSideSolidFullSquare(
                world,
                pos,
                direction.getOpposite()
        );
    }

    protected static void updatePower(World world, BlockPos pos, BlockState state, int power) {
        world.setBlockState(pos, state.with(AbstractSensorBlock.POWER, power));
        updateNeighbors(world, pos, state);
    }

    protected static void updateNeighbors(World world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        Direction facing = AbstractSensorBlock.getDirection(state);

        world.updateNeighborsAlways(pos, block, null);
        world.updateNeighborsAlways(pos.offset(facing.getOpposite()), block, null);
    }
}
