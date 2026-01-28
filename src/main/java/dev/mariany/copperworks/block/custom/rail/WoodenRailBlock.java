package dev.mariany.copperworks.block.custom.rail;

import net.minecraft.block.RailBlock;

public class WoodenRailBlock extends RailBlock implements SpeedRail, FragileRail {
    public WoodenRailBlock(Settings settings) {
        super(settings);
    }

    @Override
    public float getSpeed() {
        return 0.4F;
    }

    @Override
    public float getExhaustion() {
        return 0.3F;
    }

    @Override
    public float getBreakChance() {
        return 0.03F;
    }
}
