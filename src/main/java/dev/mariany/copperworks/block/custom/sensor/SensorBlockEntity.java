package dev.mariany.copperworks.block.custom.sensor;

import dev.mariany.copperworks.block.CWBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class SensorBlockEntity extends AbstractSensorBlockEntity{
    public SensorBlockEntity(BlockPos pos, BlockState state) {
        super(CWBlockEntities.SENSOR, pos, state, 16);
    }
}
