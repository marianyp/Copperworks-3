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
    static void insert(World world, PlayerEntity player, BlockPos pos, BlockState state) {
        List<ItemStack> stacksToDrop = Block.getDroppedStacks(state, (ServerWorld) world, pos, null);

        List<ItemStack> remainingStacks = stacksToDrop
                .stream()
                .filter(stack -> !player.getInventory().insertStack(stack))
                .toList();

        remainingStacks.forEach(stack -> Block.dropStack(world, pos, stack));
    }
}
