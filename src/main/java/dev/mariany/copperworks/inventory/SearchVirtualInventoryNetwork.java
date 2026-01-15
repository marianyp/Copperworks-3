package dev.mariany.copperworks.inventory;

import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import dev.mariany.copperworks.screen.search.SearchEntry;
import net.minecraft.item.ItemStack;

import java.util.List;

public class SearchVirtualInventoryNetwork extends VirtualInventoryNetwork {
    public SearchVirtualInventoryNetwork(InventoryNetworkScreenHandler handler) {
        super(handler);
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
        return this.handler.getNetwork().map(network -> {
            List<SearchEntry> searchResults = this.handler.getSearchResults();

            boolean changed = false;

            for (int virtualIndex = 0; virtualIndex < this.size(); virtualIndex++) {
                int index = this.handler.virtualToRealIndex(this.handler.getScrollPosition(), virtualIndex);

                if (index < searchResults.size()) {
                    int networkIndex = searchResults.get(index).getSlot();
                    ItemStack newStack = this.getStack(virtualIndex);
                    ItemStack previousStack = network.getStack(index);

                    network.setStackNoCallbacks(networkIndex, newStack);

                    if (InventoryHelper.didStackChange(previousStack, newStack)) {
                        changed = true;
                    }
                }
            }

            return changed;
        }).orElse(false);
    }
}
