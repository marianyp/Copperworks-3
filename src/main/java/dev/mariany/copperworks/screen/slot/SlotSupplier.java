package dev.mariany.copperworks.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public interface SlotSupplier {
    Slot apply(Inventory i, int index, int x, int y);
}
