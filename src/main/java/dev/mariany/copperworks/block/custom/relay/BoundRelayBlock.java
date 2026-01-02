package dev.mariany.copperworks.block.custom.relay;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.block.CWBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class BoundRelayBlock extends BlockWithEntity {
    public static final MapCodec<BoundRelayBlock> CODEC = createCodec(BoundRelayBlock::new);

    public static final IntProperty POWER = Properties.POWER;

    public BoundRelayBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWER, 0));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
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
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        super.onBroken(world, pos, state);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.getBlock() != state.getBlock() && world instanceof ServerWorld serverWorld) {
            this.update(serverWorld, pos);
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
            this.update(serverWorld, pos);
        }
    }

    protected void update(ServerWorld world, BlockPos pos) {
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

        if (otherWorld != null) {
            BlockState state = otherWorld.getBlockState(pos).withIfExists(POWER, power);
            otherWorld.setBlockState(pos, state);
            otherWorld.updateNeighbors(pos, state.getBlock());
        }
    }
}
