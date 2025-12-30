package dev.mariany.copperworks.block.clock;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.block.entity.CWBlockEntities;
import dev.mariany.copperworks.block.entity.clock.ClockBlockEntity;
import dev.mariany.copperworks.block.entity.clock.CopperClockBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CopperClockBlock extends AbstractClockBlock {
    public static final MapCodec<CopperClockBlock> CODEC = createCodec(CopperClockBlock::new);

    public CopperClockBlock(Settings settings) {
        super(settings);

        this.setDefaultState(
                this.stateManager.getDefaultState()
                                 .with(ClockBlockEntity.POWERED, false)
        );
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    @Nullable
    public CopperClockBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CopperClockBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return !world.isClient() ? validateTicker(
                type,
                CWBlockEntities.COPPER_CLOCK,
                CopperClockBlockEntity::tick
        ) : null;
    }
}
