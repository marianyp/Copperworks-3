package dev.mariany.copperworks.inventory;

import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import dev.mariany.copperworks.screen.search.SearchEntry;
import net.minecraft.item.ItemStack;

import java.util.List;

public class SearchVirtualNetworkInventory extends VirtualNetworkInventory {
    public SearchVirtualNetworkInventory(InventoryNetworkScreenHandler handler) {
        super(handler);
    }

    @Override
    protected boolean shouldUpdateRemovals() {
        return false;
    }

    @Override
    protected ItemStack getStack(float position, int virtualIndex) {
        List<SearchEntry> searchResults = this.handler.getSearchResults();

        int index = this.handler.virtualToRealIndex(position, virtualIndex);

        if (index >= 0 && index < searchResults.size()) {
            SearchEntry result = searchResults.get(index);
            return this.handler.getNetworkStack(result.getSlot());
        }

        return ItemStack.EMPTY;
    }

    @Override
    protected boolean syncStacks() {
        List<SearchEntry> searchResults = this.handler.getSearchResults();

        return this.handler.getNetwork().map(network -> {
            boolean changed = false;

            for (int virtualIndex = 0; virtualIndex < searchResults.size(); virtualIndex++) {
                int index = this.handler.virtualToRealIndex(this.handler.getScrollPosition(), virtualIndex);

                if (index >= 0 && index < searchResults.size()) {
                    int networkIndex = searchResults.get(index).getSlot();

                    ItemStack previousStack = network.getStack(networkIndex);
                    ItemStack newStack = this.getStack(virtualIndex);

                    network.setStackNoCallbacks(networkIndex, newStack);

                    if (didStackChange(previousStack, newStack)) {
                        changed = true;
                    }
                }
            }

            return changed;
        }).orElse(false);
    }
}
