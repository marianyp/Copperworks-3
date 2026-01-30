package dev.mariany.copperworks.block.custom.relay.bound;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.block.custom.relay.HighlightedRelayBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
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
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.getBlock() != state.getBlock() && world instanceof ServerWorld serverWorld) {
            update(serverWorld, pos);
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
            update(serverWorld, pos);
        }
    }

    protected static void update(ServerWorld world, BlockPos pos) {
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
