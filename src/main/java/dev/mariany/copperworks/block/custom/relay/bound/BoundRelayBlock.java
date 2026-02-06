package dev.mariany.copperworks.block.custom.relay.bound;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.block.custom.relay.HighlightedRelayBlock;
import dev.mariany.copperworks.block.custom.relay.RelayBlock;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.item.CWItems;
import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class BoundRelayBlock extends HighlightedRelayBlock<BoundRelayBlockEntity> {
    public static final MapCodec<BoundRelayBlock> CODEC = createCodec(BoundRelayBlock::new);

    public static final IntProperty POWER = Properties.POWER;

    public BoundRelayBlock(Settings settings) {
        super(settings, () -> CWBlockEntities.BOUND_RELAY);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWER, 0));
    }

    public static void initiateBinding(PlayerEntity player, ItemStack stack, BlockPos pos, Hand hand) {
        World world = player.getEntityWorld();

        ItemStack piece = ItemUsage.exchangeStack(
                stack,
                player,
                createAmethystPiece(world, pos),
                false
        );

        player.setStackInHand(hand, piece);

        playInsertSound(world, pos, false);
    }

    protected static ItemStack createAmethystPiece(World world, BlockPos pos) {
        ItemStack stack = CWItems.AMETHYST_PIECE.getDefaultStack();
        stack.set(CWComponents.RELAY_POSITION, GlobalPos.create(world.getRegistryKey(), pos));
        return stack;
    }

    public static boolean completeBinding(ServerPlayerEntity player, ItemStack stack, GlobalPos globalPos) {
        ServerWorld world = player.getEntityWorld();
        MinecraftServer server = world.getServer();

        GlobalPos otherGlobalPos = stack.get(CWComponents.RELAY_POSITION);

        if (otherGlobalPos == null || otherGlobalPos.equals(globalPos)) {
            return false;
        }

        BlockPos thisPos = globalPos.pos();
        ServerWorld thisWorld = server.getWorld(globalPos.dimension());

        BlockPos otherPos = otherGlobalPos.pos();
        ServerWorld otherWorld = server.getWorld(otherGlobalPos.dimension());

        if (thisWorld == null || otherWorld == null) {
            return false;
        }

        BlockState state = otherWorld.getBlockState(otherPos);

        if (state.getBlock() instanceof RelayBlock) {
            createBoundRelay(thisWorld, thisPos, otherGlobalPos);
            createBoundRelay(otherWorld, otherPos, globalPos);

            stack.decrement(1);
            playInsertSound(world, thisPos, true);

            return true;
        }

        return false;
    }

    protected static void createBoundRelay(ServerWorld world, BlockPos pos, GlobalPos boundPos) {
        world.setBlockState(pos, CWBlocks.BOUND_RELAY.getDefaultState());

        if (world.getBlockEntity(pos) instanceof BoundRelayBlockEntity boundRelayBlockEntity) {
            boundRelayBlockEntity.bind(boundPos);
        }
    }

    protected static void playInsertSound(World world, BlockPos pos, boolean completed) {
        world.playSound(
                null,
                pos,
                CWSoundEvents.BLOCK_RELAY_INSERT,
                SoundCategory.BLOCKS,
                1,
                completed ? 1 : 1.6F
        );
    }

    @Override
    protected MapCodec<? extends BoundRelayBlock> getCodec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BoundRelayBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BoundRelayBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWER);
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
        return state.get(POWER);
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
            update(serverWorld, pos);
        }
    }

    public static void update(ServerWorld world, BlockPos pos) {
        if (canRelay(world, pos) && world.getBlockEntity(pos) instanceof BoundRelayBlockEntity boundRelayBlockEntity) {
            boundRelayBlockEntity.getBoundPos().ifPresent(
                    boundPos -> updateRelay(
                            world,
                            boundPos,
                            getRedstonePower(world, pos)
                    )
            );
        }
    }

    protected static boolean canRelay(World world, BlockPos pos) {
        return world.getBlockState(pos).get(POWER, 0) <= 0;
    }

    protected static int getRedstonePower(World world, BlockPos pos) {
        return world.getReceivedRedstonePower(pos);
    }

    private static void updateRelay(ServerWorld world, GlobalPos globalPos, int power) {
        MinecraftServer server = world.getServer();
        ServerWorld otherWorld = server.getWorld(globalPos.dimension());
        BlockPos pos = globalPos.pos();

        if (otherWorld != null && otherWorld.isPosLoaded(pos)) {
            BlockState state = otherWorld.getBlockState(pos);
            boolean changed = state.get(POWER, 0) != power;

            if (changed) {
                otherWorld.setBlockState(pos, state.withIfExists(POWER, power));
                otherWorld.updateNeighbors(pos, state.getBlock());
                update(world, pos);
            }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient() && player.getMainHandStack().isEmpty() && player.getOffHandStack().isEmpty()) {
            if (world.getBlockEntity(pos) instanceof BoundRelayBlockEntity boundRelayBlockEntity) {
                boundRelayBlockEntity.focus(true);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }
}
