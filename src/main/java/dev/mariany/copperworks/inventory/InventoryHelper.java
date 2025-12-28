package dev.mariany.copperworks.inventory;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface InventoryHelper {
    static List<ItemStack> copy(World world, BlockPos pos, boolean unset) {
        List<ItemStack> stacks = new ArrayList<>();

        if (world.getBlockEntity(pos) instanceof Inventory inventory) {
            for (int slot = 0; slot < inventory.size(); slot++) {
                ItemStack stack = inventory.getStack(slot);
                stacks.add(stack.copy());

                if (unset && !stack.isEmpty()) {
                    inventory.setStack(slot, ItemStack.EMPTY);
                }
            }
        }

        return stacks;
    }

    static List<ItemStack> addAll(World world, BlockPos pos, List<ItemStack> stacks) {
        List<ItemStack> overflow = new ArrayList<>();

        if (world.getBlockEntity(pos) instanceof Inventory inventory) {
            for (int slot = 0; slot < stacks.size(); slot++) {
                Set<Integer> exploredSlots = new HashSet<>();
                ItemStack stack = stacks.get(slot);
                int maxSlots = inventory.size();
                int currentSlot = slot;

                while (currentSlot >= 0) {
                    if (exploredSlots.size() >= maxSlots) {
                        overflow.add(stack);
                        break;
                    }

                    ItemStack slotStack = inventory.getStack(currentSlot);

                    if (slotStack.isEmpty()) {
                        inventory.setStack(currentSlot, stack);
                        break;
                    } else if (canCombine(stack, slotStack)) {
                        moveCountInto(slotStack, stack);
                    }

                    exploredSlots.add(currentSlot);

                    // Increment and wrap
                    currentSlot = (currentSlot + 1) % (maxSlots + 1);
                }
            }

            inventory.markDirty();
        }

        return overflow;
    }

    static void scatterItems(World world, BlockPos pos, List<ItemStack> stacks) {
        stacks.forEach(stack -> ItemScatterer.spawn(
                world,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                stack
        ));
    }

    static int shiftLeft(InventoryNetwork network, int slot) {
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
    private static ItemStack getNonEmptyStack(InventoryNetwork network, int slot) {
        ItemStack stack = network.getStack(slot);
        return stack.isEmpty() ? null : stack;
    }

    private static boolean tryMoveIntoSlot(
            InventoryNetwork network,
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
            InventoryNetwork network,
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

    private static void syncSourceSlot(InventoryNetwork network, int sourceSlot, ItemStack sourceStack) {
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
