package dev.mariany.copperworks.block.custom;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.block.CWBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class InventoryNetworkBlock extends BlockWithEntity {
    public InventoryNetworkBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected abstract MapCodec<? extends InventoryNetworkBlock> getCodec();

    @Override
    @Nullable
    public abstract InventoryNetworkBlockEntity createBlockEntity(BlockPos pos, BlockState state);

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof InventoryNetworkBlockEntity inventoryNetworkBlockEntity) {
            player.incrementStat(this.getOpenStat());
            inventoryNetworkBlockEntity.interact(player, pos);
        }

        return ActionResult.SUCCESS;
    }

    protected abstract Stat<Identifier> getOpenStat();

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (world.getBlockEntity(pos) instanceof InventoryNetworkBlockEntity inventoryNetworkBlockEntity) {
            inventoryNetworkBlockEntity.onBlockAdded(world, pos);
        }
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        world.updateComparators(pos, state.getBlock());
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
                CWBlockEntities.COPPER_BARREL,
                (tickWorld, pos, tickState, copperBarrelBlockEntity) ->
                        copperBarrelBlockEntity.tick()
        ) : null;
    }
}
