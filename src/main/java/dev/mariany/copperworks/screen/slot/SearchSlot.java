package dev.mariany.copperworks.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SearchSlot extends Slot {
    public SearchSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canBeHighlighted() {
        return this.hasStack();
    }

    @Override
    public boolean isEnabled() {
        return this.hasStack();
    }
}
