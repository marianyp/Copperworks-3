package dev.mariany.copperworks.inventory;

import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public class VirtualNetworkInventory extends SimpleInventory implements InventoryChangedListener {
    private final InventoryNetworkScreenHandler handler;

    public VirtualNetworkInventory(InventoryNetworkScreenHandler handler) {
        super(handler.getColumns() * handler.getRows());
        this.handler = handler;
        this.addListener(this);
    }

    public void setStackNoCallbacks(int slot, ItemStack stack) {
        this.heldStacks.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
    }

    protected void syncStacks() {
        this.handler.getNetwork().ifPresent(network -> {
            for (int index = 0; index < this.size(); index++) {
                int networkIndex = this.handler.visibleToNetworkIndex(this.handler.getScrollPosition(), index);

                if (networkIndex >= 0 && networkIndex < network.size()) {
                    network.setStack(networkIndex, this.getStack(index));
                }
            }
        });
    }

    @Override
    public void onInventoryChanged(Inventory sender) {
        this.syncStacks();
    }
}
