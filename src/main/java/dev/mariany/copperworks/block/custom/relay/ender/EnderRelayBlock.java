package dev.mariany.copperworks.block.custom.relay.ender;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EnderRelayBlock extends BlockWithEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final MapCodec<EnderRelayBlock> CODEC = createCodec(EnderRelayBlock::new);

    public static final BooleanProperty POWERED = Properties.POWERED;

    public EnderRelayBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(POWERED, false));
    }

    public static void completeBinding(ServerWorld world, BlockPos pos, Entity entity, ItemStack stack) {
        if (stack.isEmpty()) {
            LOGGER.error("Attempted to complete binding of Ender Relay using empty stack");
            return;
        }

        world.setBlockState(pos, CWBlocks.ENDER_RELAY.getDefaultState());

        if (world.getBlockEntity(pos) instanceof EnderRelayBlockEntity enderRelayBlockEntity) {
            enderRelayBlockEntity.setBindingStack(stack.copyWithCount(1));
            stack.decrementUnlessCreative(1, entity instanceof PlayerEntity player ? player : null);

            enderRelayBlockEntity.setOwner(entity);

            playBindSound(world, pos);
        } else {
            LOGGER.error("Attempted to complete binding of Ender Relay, but couldn't place block entity");
        }
    }

    protected static void playBindSound(World world, BlockPos pos) {
        world.playSound(null, pos, CWSoundEvents.BLOCK_ENDER_RELAY_BIND, SoundCategory.BLOCKS);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    @Nullable
    public EnderRelayBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnderRelayBlockEntity(pos, state);
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
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return !world.isClient() ? validateTicker(
                type,
                CWBlockEntities.ENDER_RELAY,
                (tickWorld, pos, tickState, enderRelayBlockEntity) ->
                        enderRelayBlockEntity.tick(tickWorld)
        ) : null;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(POWERED) && !world.isReceivingRedstonePower(pos)) {
            setPowered(world, pos, false);
        }
    }

    @Override
    protected void neighborUpdate(
            BlockState state,
            World world,
            BlockPos pos,
            Block sourceBlock,
            @Nullable WireOrientation wireOrientation,
            boolean notify
    ) {
        if (world instanceof ServerWorld serverWorld) {
            this.update(serverWorld, pos, state);
        }
    }

    public void update(ServerWorld world, BlockPos pos, BlockState state) {
        boolean receivingPower = world.isReceivingRedstonePower(pos);

        if (receivingPower != state.get(POWERED)) {
            if (receivingPower) {
                setPowered(world, pos, true);

                if (world.getBlockEntity(pos) instanceof EnderRelayBlockEntity enderRelayBlockEntity) {
                    enderRelayBlockEntity.teleport();
                }
            }

            this.scheduleTick(world, pos);
        }
    }

    protected static void setPowered(World world, BlockPos pos, boolean powered) {
        BlockState state = world.getBlockState(pos).withIfExists(POWERED, powered);
        world.setBlockState(pos, state);
    }

    protected void scheduleTick(World world, BlockPos pos) {
        if (!world.isClient() && !world.getBlockTickScheduler().isQueued(pos, this)) {
            world.scheduleBlockTick(pos, this, 20);
        }
    }
}
