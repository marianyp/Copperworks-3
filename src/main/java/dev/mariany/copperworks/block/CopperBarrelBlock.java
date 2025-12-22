package dev.mariany.copperworks.block;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.block.entity.CWBlockEntities;
import dev.mariany.copperworks.block.entity.CopperBarrelBlockEntity;
import dev.mariany.copperworks.inventory.InventoryNetwork;
import dev.mariany.copperworks.inventory.NetworkState;
import dev.mariany.copperworks.packet.clientbound.InventoryNetworkUpdate;
import dev.mariany.copperworks.stat.CWStats;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class CopperBarrelBlock extends BlockWithEntity {
    public static final MapCodec<CopperBarrelBlock> CODEC = createCodec(CopperBarrelBlock::new);
    public static final EnumProperty<Direction> FACING = Properties.FACING;

    public CopperBarrelBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CopperBarrelBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return !world.isClient() ? validateTicker(type, CWBlockEntities.COPPER_BARREL, CopperBarrelBlockEntity::tick) :
                null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof CopperBarrelBlockEntity copperBarrelBlockEntity) {
            player.incrementStat(CWStats.OPEN_COPPER_BARREL);

            if (world instanceof ServerWorld serverWorld) {
                GlobalPos globalPos = GlobalPos.create(world.getRegistryKey(), pos);
                InventoryNetwork network = copperBarrelBlockEntity.getNetwork();

                if (player instanceof NetworkState networkState) {
                    networkState.copperworks2$setNetwork(copperBarrelBlockEntity.getNetwork());
                }

                if (player instanceof ServerPlayerEntity serverPlayer) {
                    ServerPlayNetworking.send(serverPlayer, new InventoryNetworkUpdate(globalPos, network));
                }

                PiglinBrain.onGuardedBlockInteracted(serverWorld, player, true);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (world.getBlockEntity(pos) instanceof CopperBarrelBlockEntity copperBarrelBlockEntity) {
            Set<BlockPos> discoveredControllers = new HashSet<>();

            for (Direction direction : Direction.values()) {
                BlockPos offsetPos = pos.offset(direction);
                BlockEntity neighboringBlockEntity = world.getBlockEntity(offsetPos);

                if (neighboringBlockEntity instanceof CopperBarrelBlockEntity otherCopperBarrelBlockEntity) {
                    InventoryNetwork network = otherCopperBarrelBlockEntity.getNetwork();

                    network.getControllerPos().ifPresent(controllerPos -> {
                        copperBarrelBlockEntity.connect(
                                world,
                                otherCopperBarrelBlockEntity,
                                !discoveredControllers.contains(controllerPos)
                        );

                        discoveredControllers.add(controllerPos);
                    });
                }
            }
        }
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);
    }
}
