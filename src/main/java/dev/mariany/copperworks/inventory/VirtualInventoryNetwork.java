package dev.mariany.copperworks.inventory;

import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public abstract class VirtualInventoryNetwork extends SimpleInventory implements InventoryChangedListener {
    protected final InventoryNetworkScreenHandler handler;
    protected boolean prepared = false;

    public VirtualInventoryNetwork(InventoryNetworkScreenHandler handler) {
        super(handler.getColumns() * handler.getRows());
        this.handler = handler;
        this.addListener(this);
    }

    public void setPrepared() {
        this.prepared = true;
    }

    public void scrollItems(float position) {
        int columns = this.handler.getColumns();
        int rows = this.handler.getRows();

        for (int virtualIndex = 0; virtualIndex < columns * rows; virtualIndex++) {
            this.setStackNoCallbacks(virtualIndex, this.getStack(position, virtualIndex).copy());
        }
    }

    public void setStackNoCallbacks(int slot, ItemStack stack) {
        this.heldStacks.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
    }

    abstract protected ItemStack getStack(float position, int virtualIndex);

    abstract protected boolean syncStacks();

    @Override
    public void markDirty() {
        if (this.prepared) {
            super.markDirty();
        }
    }

    @Override
    public void onInventoryChanged(Inventory sender) {
        if (this.syncStacks()) {
            this.handler.getNetwork().ifPresent(InventoryNetwork::markDirty);
        }
    }
}
