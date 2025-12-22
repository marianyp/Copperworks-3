package dev.mariany.copperworks.screen;

import dev.mariany.copperworks.inventory.NetworkState;
import dev.mariany.copperworks.inventory.StorageNetwork;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;

public class CopperBarrelScreenHandler extends ScreenHandler {
    private static final int SLOT_BOX_SIZE = 18;
    private static final int SLOT_SIZE = 16;

    protected final int columns;
    protected final int maxRows;

    protected PlayerEntity player;

    public CopperBarrelScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, 8, 6);
    }

    public CopperBarrelScreenHandler(int syncId, PlayerInventory playerInventory, int columns, int maxRows) {
        super(CWScreenHandlers.COPPER_BARREL, syncId);

        this.columns = columns;
        this.maxRows = maxRows;
        this.player = playerInventory.player;

        this.getNetwork().ifPresent(network -> {
            network.onOpen(this.player);
            this.addInventorySlots(network);
        });

        this.addPlayerSlots(
                playerInventory,
                SLOT_SIZE / 2,
                SLOT_BOX_SIZE + this.getRows() * SLOT_BOX_SIZE + 13
        );
    }

    public Optional<StorageNetwork> getNetwork() {
        if (this.player instanceof NetworkState networkState) {
            return networkState.copperworks2$getNetwork();
        }

        return Optional.empty();
    }

    public int getRows() {
        int slots = this.getNetwork().map(StorageNetwork::size).orElse(0);
        return Math.min(MathHelper.ceil((float) slots / this.columns), this.maxRows);
    }

    protected void addInventorySlots(Inventory inventory) {
        for (int y = 0; y < this.getRows(); y++) {
            for (int x = 0; x < this.columns; x++) {
                int index = x + y * this.columns;
                int slotX = (SLOT_SIZE / 2) + x * SLOT_BOX_SIZE;
                int slotY = CopperBarrelScreenHandler.SLOT_BOX_SIZE + y * SLOT_BOX_SIZE;

                this.addSlot(new Slot(inventory, index, slotX, slotY));
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        int rows = this.getRows();

        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            resultStack = slotStack.copy();

            if (slotIndex < this.getRows() * this.columns) {
                if (!this.insertItem(slotStack, rows * this.columns, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(slotStack, 0, rows * this.columns, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return resultStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.getNetwork()
                   .map(network -> network.canPlayerUse(player))
                   .orElse(false);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        if (player instanceof NetworkState networkState) {
            networkState.copperworks2$setNetwork(null);
        }
    }
}
