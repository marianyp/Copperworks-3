package dev.mariany.copperworks.block.custom.relay.radio;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.block.custom.relay.HighlightedRelayBlock;
import dev.mariany.copperworks.item.custom.radio.RadioItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class RadioRelayBlock extends HighlightedRelayBlock<RadioRelayBlockEntity> {
    public static final MapCodec<RadioRelayBlock> CODEC = createCodec(RadioRelayBlock::new);

    public static final BooleanProperty POWERED = Properties.POWERED;

    public RadioRelayBlock(Settings settings) {
        super(settings, () -> CWBlockEntities.RADIO_RELAY);
        this.setDefaultState(this.getDefaultState().with(POWERED, false));
    }

    public int getPulseDuration() {
        return 10;
    }

    @Override
    protected MapCodec<? extends RadioRelayBlock> getCodec() {
        return CODEC;
    }

    @Override
    @Nullable
    public RadioRelayBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RadioRelayBlockEntity(pos, state);
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

    @Override
    protected ActionResult onUseWithItem(
            ItemStack stack,
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit
    ) {
        if (stack.getItem() instanceof RadioItem) {
            RadioItem.completeBinding(world, pos, stack);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
