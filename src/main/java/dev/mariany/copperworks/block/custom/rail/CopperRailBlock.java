package dev.mariany.copperworks.block.custom.rail;

import net.minecraft.block.RailBlock;

public class CopperRailBlock extends RailBlock implements SpeedRail {
    public CopperRailBlock(Settings settings) {
        super(settings);
    }

    @Override
    public float getSpeed() {
        return 1;
    }

    @Override
    public float getExhaustion() {
        return 0.1F;
    }
}
