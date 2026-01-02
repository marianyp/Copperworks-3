package dev.mariany.copperworks.block.custom.relay;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.CWBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class RadioBoundRelayBlock extends AbstractRelayBlock<RadioBoundRelayBlockEntity> {
    public static final MapCodec<RadioBoundRelayBlock> CODEC = createCodec(RadioBoundRelayBlock::new);

    public static final BooleanProperty POWERED = Properties.POWERED;

    public RadioBoundRelayBlock(Settings settings) {
        super(settings, () -> CWBlockEntities.RADIO_BOUND_RELAY);
        this.setDefaultState(this.getDefaultState().with(POWERED, false));
    }

    public int getPulseDuration() {
        return 10;
    }

    @Override
    protected MapCodec<? extends RadioBoundRelayBlock> getCodec() {
        return CODEC;
    }

    @Override
    @Nullable
    public RadioBoundRelayBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RadioBoundRelayBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return CWBlocks.RELAY.asItem().getDefaultStack();
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) ? 15 : 0;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, state.withIfExists(POWERED, false));
    }
}
