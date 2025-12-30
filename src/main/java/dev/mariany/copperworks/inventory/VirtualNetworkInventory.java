package dev.mariany.copperworks.inventory;

import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public abstract class VirtualNetworkInventory extends SimpleInventory implements InventoryChangedListener {
    protected final InventoryNetworkScreenHandler handler;

    public VirtualNetworkInventory(InventoryNetworkScreenHandler handler) {
        super(handler.getColumns() * handler.getRows());
        this.handler = handler;
        this.addListener(this);
    }

    public void scrollItems(float position) {
        int columns = this.handler.getColumns();
        int rows = this.handler.getRows();

        for (int virtualIndex = 0; virtualIndex < columns * rows; virtualIndex++) {
            this.setStackNoCallbacks(virtualIndex, this.getStack(position, virtualIndex));
        }
    }

    protected void setStackNoCallbacks(int slot, ItemStack stack) {
        this.heldStacks.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
    }

    protected boolean didStackChange(ItemStack previousStack, ItemStack newStack) {
        boolean sameCount = previousStack.getCount() == newStack.getCount();
        boolean sameItems = ItemStack.areItemsAndComponentsEqual(previousStack, newStack);

        return !sameCount || !sameItems;
    }

    abstract protected ItemStack getStack(float position, int virtualIndex);

    abstract protected boolean syncStacks();

    abstract protected boolean shouldUpdateRemovals();

    @Override
    public void onInventoryChanged(Inventory sender) {
        if (this.syncStacks()) {
            this.handler.getNetwork().ifPresent(InventoryNetwork::markDirty);
        }
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = super.removeStack(slot, amount);

        if(!result.isEmpty() && this.shouldUpdateRemovals()) {
            this.handler.getNetwork().ifPresent(InventoryNetwork::markDirty);
        }

        return result;
    }
}
