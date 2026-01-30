package dev.mariany.copperworks.block;

import dev.mariany.copperworks.sound.SoundHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public interface SelfInserting {
    static void insert(
            World world,
            PlayerEntity player,
            BlockPos pos,
            List<ItemStack> stacks
    ) {
        List<ItemStack> stacksCopy = stacks.stream().map(ItemStack::copy).toList();

        List<ItemStack> remainingStacks = stacks
                .stream()
                .filter(stack -> !player.getInventory().insertStack(stack))
                .toList();

        boolean inserted = false;

        int originalDropCount = stacksCopy.size();
        int remainingDropCount = remainingStacks.size();

        if (originalDropCount != remainingDropCount) {
            inserted = true;
        } else {
            for (int i = 0; i < originalDropCount; i++) {
                ItemStack originalStack = stacksCopy.get(i);
                ItemStack remainingStack = remainingStacks.get(i);

                if (originalStack.getCount() != remainingStack.getCount()) {
                    inserted = true;
                    break;
                }

                if (!ItemStack.areItemsAndComponentsEqual(originalStack, remainingStack)) {
                    inserted = true;
                    break;
                }
            }
        }

        if (inserted) {
            SoundHelper.playSoundToPlayer(
                    player,
                    SoundEvents.ENTITY_ITEM_PICKUP,
                    SoundCategory.PLAYERS,
                    0.2F,
                    (world.random.nextFloat() - world.random.nextFloat()) * 1.4F + 2
            );
        }

        remainingStacks.forEach(stack -> Block.dropStack(world, pos, stack));
    }
}