package dev.mariany.copperworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SelfInserting {
    static void insert(
            World world,
            PlayerEntity player,
            BlockPos pos,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool
    ) {
        if (world instanceof ServerWorld serverWorld) {
            ComponentMap componentMap = blockEntity == null ? null : blockEntity.createComponentMap();

            List<ItemStack> stacksToDrop = Block.getDroppedStacks(
                    state,
                    serverWorld,
                    pos,
                    null,
                    player,
                    tool
            );

            if(componentMap != null) {
                stacksToDrop.forEach(stack -> stack.applyComponentsFrom(componentMap));
            }

            List<ItemStack> remainingStacks = stacksToDrop
                    .stream()
                    .filter(stack -> !player.getInventory().insertStack(stack))
                    .toList();

            remainingStacks.forEach(stack -> Block.dropStack(world, pos, stack));
        }
    }
}
