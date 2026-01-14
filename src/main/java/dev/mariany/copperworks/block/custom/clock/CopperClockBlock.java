package dev.mariany.copperworks.block.custom.clock;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.stat.CWStats;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public class CopperClockBlock extends AbstractClockBlock {
    public static final MapCodec<CopperClockBlock> CODEC = createCodec(CopperClockBlock::new);

    public CopperClockBlock(Settings settings) {
        super(settings, 10, 10);
    }

    @Override
    protected MapCodec<? extends AbstractClockBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected Stat<Identifier> getInteractStat() {
        return Stats.CUSTOM.getOrCreateStat(CWStats.INTERACT_WITH_COPPER_CLOCK);
    }
}
