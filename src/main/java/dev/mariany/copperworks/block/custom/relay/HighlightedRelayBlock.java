package dev.mariany.copperworks.block.custom.relay;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class HighlightedRelayBlock<E extends HighlightedBlockEntity> extends BlockWithEntity {
    private final Supplier<BlockEntityType<? extends E>> entityTypeRetriever;

    protected HighlightedRelayBlock(Settings settings, Supplier<BlockEntityType<? extends E>> entityTypeRetriever) {
        super(settings);
        this.entityTypeRetriever = entityTypeRetriever;
    }

    @Override
    protected abstract MapCodec<? extends HighlightedRelayBlock<E>> getCodec();

    @Override
    public abstract HighlightedBlockEntity createBlockEntity(BlockPos pos, BlockState state);

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return world.isClient() ?
                validateTicker(type, this.getExpectedEntityType(), HighlightedBlockEntity::clientTick) :
                null;
    }

    private BlockEntityType<? extends HighlightedBlockEntity> getExpectedEntityType() {
        return this.entityTypeRetriever.get();
    }
}
