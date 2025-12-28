package dev.mariany.copperworks.screen;

import dev.mariany.copperworks.inventory.InventoryNetwork;
import dev.mariany.copperworks.inventory.InventoryNetworkState;
import dev.mariany.copperworks.inventory.VirtualNetworkInventory;
import dev.mariany.copperworks.packet.clientbound.InventoryScrollValidationPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class InventoryNetworkScreenHandler extends ScreenHandler implements ScrollableInventory {
    public static final int SLOT_BOX_SIZE = 18;
    public static final int SLOT_SIZE = 16;
    public static final int INVENTORY_Y_OFFSET = 13;

    protected final int columns;
    protected final int maxRows;

    protected final PlayerEntity player;
    protected final VirtualNetworkInventory virtualInventory;

    protected float scrollPosition;

    @Nullable
    protected Float validatedScrollPosition;

    public InventoryNetworkScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, 8, 6);
    }

    public InventoryNetworkScreenHandler(int syncId, PlayerInventory playerInventory, int columns, int maxRows) {
        super(CWScreenHandlers.INVENTORY_NETWORK, syncId);

        this.columns = columns;
        this.maxRows = maxRows;
        this.player = playerInventory.player;
        this.virtualInventory = new VirtualNetworkInventory(this);

        this.updateScrollPosition(0);

        this.addVirtualSlots();

        this.addPlayerSlots(
                playerInventory,
                SLOT_SIZE / 2,
                INVENTORY_Y_OFFSET + SLOT_BOX_SIZE + this.getRows() * SLOT_BOX_SIZE + 13
        );
    }

    public int getColumns() {
        return this.columns;
    }

    public int getMaxRows() {
        return this.maxRows;
    }

    public int getRows() {
        int slots = this.getNetwork().map(InventoryNetwork::size).orElse(0);
        return Math.min(MathHelper.ceil((float) slots / this.getColumns()), this.getMaxRows());
    }

    protected int getOverflowRows() {
        return MathHelper.ceilDiv(this.getNetworkSize(), this.getColumns()) - this.getMaxRows();
    }

    public int getRow(float scroll) {
        return Math.max((int) (scroll * this.getOverflowRows() + 0.5), 0);
    }

    public float getScrollPosition() {
        return this.scrollPosition;
    }

    public boolean isScrollerEnabled() {
        return this.getNetworkSize() > this.getColumns() * this.getMaxRows();
    }

    public VirtualNetworkInventory getVirtualInventory() {
        return this.virtualInventory;
    }

    public int getNetworkSize() {
        return this.getNetwork().map(InventoryNetwork::size).orElse(0);
    }

    public ItemStack getNetworkStack(int slotIndex) {
        return this.getNetwork().map(network -> network.getStack(slotIndex)).orElse(ItemStack.EMPTY);
    }

    public Optional<InventoryNetwork> getNetwork() {
        if (this.player instanceof InventoryNetworkState networkState) {
            return networkState.copperworks2$getNetwork();
        }

        return Optional.empty();
    }

    public void updateScrollPosition(float scrollPosition) {
        this.scrollPosition = scrollPosition;
        this.scrollItems(scrollPosition);
    }

    public float calculateScrollPosition(int row) {
        return MathHelper.clamp((float) row / this.getOverflowRows(), 0, 1);
    }

    public float calculateScrollPosition(float current, double amount) {
        return MathHelper.clamp(current - (float) (amount / this.getOverflowRows()), 0, 1);
    }

    public int visibleToNetworkIndex(float position, int visibleIndex) {
        int columns = this.getColumns();
        int scrollOffsetRows = this.getRow(position);

        int column = visibleIndex % columns;
        int row = visibleIndex / columns;

        return column + (row + scrollOffsetRows) * columns;
    }

    public void scrollItems(float position) {
        int visibleCount = this.getColumns() * this.getRows();

        for (int visibleIndex = 0; visibleIndex < visibleCount; visibleIndex++) {
            int slotIndex = this.visibleToNetworkIndex(position, visibleIndex);

            ItemStack stack = (slotIndex >= 0 && slotIndex < this.getNetworkSize())
                    ? this.getNetworkStack(slotIndex)
                    : ItemStack.EMPTY;

            this.virtualInventory.setStackNoCallbacks(visibleIndex, stack);
        }

        this.virtualInventory.markDirty();
    }

    protected void addVirtualSlots() {
        for (int y = 0; y < this.getRows(); y++) {
            for (int x = 0; x < this.getColumns(); x++) {
                int index = x + y * this.getColumns();
                int slotX = (SLOT_SIZE / 2) + x * SLOT_BOX_SIZE;
                int slotY = INVENTORY_Y_OFFSET + SLOT_BOX_SIZE + y * SLOT_BOX_SIZE;

                this.addSlot(new Slot(this.virtualInventory, index, slotX, slotY));
            }
        }
    }

    @Override
    public void syncState() {
        if (this.player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(
                    serverPlayer,
                    new InventoryScrollValidationPacket(this.syncId, this.scrollPosition)
            );
        }

        super.syncState();
    }

    @Override
    public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
        if (this.validatedScrollPosition != null && this.validatedScrollPosition == this.scrollPosition) {
            super.updateSlotStacks(revision, stacks, cursorStack);
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        int columns = this.getColumns();
        int rows = this.getRows();

        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            resultStack = slotStack.copy();

            if (slotIndex < rows * columns) {
                if (!this.insertItem(slotStack, rows * columns, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(slotStack, 0, rows * columns, false)) {
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

        if (player instanceof InventoryNetworkState networkState) {
            networkState.copperworks2$setNetwork(null);
        }
    }

    @Override
    public void onScroll(float scrollPosition) {
        this.updateScrollPosition(scrollPosition);
        this.scrollItems(this.getScrollPosition());
    }

    @Override
    public void onScrollValidation(float scrollPosition) {
        this.validatedScrollPosition = scrollPosition;
    }
}
