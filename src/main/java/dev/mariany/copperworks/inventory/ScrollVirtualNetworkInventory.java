package dev.mariany.copperworks.inventory;

import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import net.minecraft.item.ItemStack;

public class ScrollVirtualNetworkInventory extends VirtualNetworkInventory {
    public ScrollVirtualNetworkInventory(InventoryNetworkScreenHandler handler) {
        super(handler);
    }

    @Override
    protected ItemStack getStack(float position, int virtualIndex) {
        int index = this.handler.virtualToRealIndex(position, virtualIndex);

        return index >= 0 && index < this.handler.getNetworkSize()
                ? this.handler.getNetworkStack(index)
                : ItemStack.EMPTY;
    }

    @Override
    protected boolean syncStacks() {
        return this.handler.getNetwork().map(network -> {
            boolean changed = false;

            for (int virtualIndex = 0; virtualIndex < this.size(); virtualIndex++) {
                int index = this.handler.virtualToRealIndex(this.handler.getScrollPosition(), virtualIndex);

                if (index >= 0 && index < network.size()) {
                    ItemStack previousStack = network.getStack(index);
                    ItemStack newStack = this.getStack(virtualIndex);

                    network.setStackNoCallbacks(index, newStack);

                    if (InventoryHelper.didStackChange(previousStack, newStack)) {
                        changed = true;
                    }
                }
            }

            return changed;
        }).orElse(false);
    }
}
