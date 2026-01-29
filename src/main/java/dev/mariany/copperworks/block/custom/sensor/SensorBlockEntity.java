package dev.mariany.copperworks.block.custom.sensor;

import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class SensorBlockEntity extends AbstractSensorBlockEntity {
    public SensorBlockEntity(BlockPos pos, BlockState state) {
        super(CWBlockEntities.SENSOR, pos, state, 16);
    }

    @Override
    protected void playSound(ServerWorld serverWorld, BlockPos pos) {
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
