package dev.mariany.copperworks.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public interface SlotSupplier {
    Slot apply(Inventory inventory, int index, int x, int y);
}
