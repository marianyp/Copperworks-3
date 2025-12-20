package dev.mariany.copperworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public interface SelfInserting {
    static void insert(World world, PlayerEntity player, BlockPos pos, BlockState state, ItemStack tool) {
        if (world instanceof ServerWorld serverWorld) {
            List<ItemStack> stacksToDrop = Block.getDroppedStacks(
                    state,
                    serverWorld,
                    pos,
                    null,
                    player,
                    tool
            );

            List<ItemStack> remainingStacks = stacksToDrop
                    .stream()
                    .filter(stack -> !player.getInventory().insertStack(stack))
                    .toList();

            remainingStacks.forEach(stack -> Block.dropStack(world, pos, stack));
        }
    }
}
