package dev.mariany.copperworks.inventory;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface StorageNetworkHelper {
    static int shiftLeft(StorageNetwork network, int slot) {
        if (slot <= 0) {
            return slot;
        }

        ItemStack stack = getNonEmptyStack(network, slot);

        if (stack == null) {
            return slot;
        }

        int leftSlot = slot - 1;

        if (tryMoveIntoSlot(network, leftSlot, slot, stack)) {
            return leftSlot;
        }

        if (tryMergeIntoSlot(network, leftSlot, slot, stack)) {
            return stack.isEmpty() ? leftSlot : slot;
        }

        if (shiftLeft(network, leftSlot) == leftSlot) {
            return slot;
        }

        if (tryMoveIntoSlot(network, leftSlot, slot, stack)) {
            return leftSlot;
        }

        if (tryMergeIntoSlot(network, leftSlot, slot, stack)) {
            return stack.isEmpty() ? leftSlot : slot;
        }

        return slot;
    }

    @Nullable
    private static ItemStack getNonEmptyStack(StorageNetwork network, int slot) {
        ItemStack stack = network.getStack(slot);
        return stack.isEmpty() ? null : stack;
    }

    private static boolean tryMoveIntoSlot(
            StorageNetwork network,
            int targetSlot,
            int sourceSlot,
            ItemStack sourceStack
    ) {
        if (!network.getStack(targetSlot).isEmpty()) {
            return false;
        }

        network.heldStacks.remove(sourceSlot);
        network.heldStacks.put(targetSlot, sourceStack);

        return true;
    }

    private static boolean tryMergeIntoSlot(
            StorageNetwork network,
            int targetSlot,
            int sourceSlot,
            ItemStack sourceStack
    ) {
        ItemStack targetStack = network.getStack(targetSlot);

        if (!canCombine(targetStack, sourceStack)) {
            return false;
        }

        int moved = moveCountInto(targetStack, sourceStack);

        if (moved == 0) {
            return false;
        }

        network.heldStacks.put(targetSlot, targetStack);
        syncSourceSlot(network, sourceSlot, sourceStack);

        return true;
    }

    private static int moveCountInto(ItemStack target, ItemStack source) {
        int space = target.getMaxCount() - target.getCount();

        if (space <= 0) {
            return 0;
        }

        int moved = Math.min(space, source.getCount());
        target.setCount(target.getCount() + moved);
        source.setCount(source.getCount() - moved);

        return moved;
    }

    private static void syncSourceSlot(StorageNetwork network, int sourceSlot, ItemStack sourceStack) {
        if (sourceStack.isEmpty()) {
            network.heldStacks.remove(sourceSlot);
        } else {
            network.heldStacks.put(sourceSlot, sourceStack);
        }
    }

    private static boolean canCombine(ItemStack firstStack, ItemStack secondStack) {
        if (firstStack.isEmpty() || secondStack.isEmpty()) {
            return false;
        }

        return ItemStack.areItemsAndComponentsEqual(firstStack, secondStack);
    }
}
