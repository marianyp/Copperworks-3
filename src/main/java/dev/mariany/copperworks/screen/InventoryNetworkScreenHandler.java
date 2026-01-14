package dev.mariany.copperworks.screen;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.custom.InventoryNetworkBlockEntity;
import dev.mariany.copperworks.inventory.*;
import dev.mariany.copperworks.mixin.accessor.ScreenHandlerAccessor;
import dev.mariany.copperworks.packet.clientbound.InventoryNetworkUpdatePacket;
import dev.mariany.copperworks.packet.clientbound.InventoryValidationPacket;
import dev.mariany.copperworks.screen.scroll.ScrollableInventory;
import dev.mariany.copperworks.screen.search.SearchEntry;
import dev.mariany.copperworks.screen.search.SearchableInventory;
import dev.mariany.copperworks.screen.slot.SearchSlot;
import dev.mariany.copperworks.screen.slot.SlotSupplier;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InventoryNetworkScreenHandler extends ScreenHandler implements ScrollableInventory, SearchableInventory,
        InventoryChangedListener {
    public static final int SLOT_BOX_SIZE = 18;
    public static final int SLOT_SIZE = 16;
    public static final int INVENTORY_Y_OFFSET = 13;

    protected final int columns;
    protected final int maxRows;

    protected final PlayerEntity player;

    @Nullable
    protected final InventoryNetworkBlockEntity inventoryNetworkBlockEntity;

    protected VirtualNetworkInventory virtualNetworkInventory;

    protected float scrollPosition;

    protected final List<SearchEntry> searchEntries = new ArrayList<>();
    protected final List<SearchEntry> searchResults = new ArrayList<>();
    protected final List<ItemStack> searchResultStacks = new ArrayList<>();

    @Nullable
    protected String searchQuery = null;

    @Nullable
    protected Float validatedScrollPosition;

    @Nullable
    protected String validatedSearchQuery;

    public InventoryNetworkScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, null, playerInventory);
    }

    public InventoryNetworkScreenHandler(
            int syncId,
            @Nullable InventoryNetworkBlockEntity inventoryNetworkBlockEntity,
            PlayerInventory playerInventory
    ) {
        this(syncId, playerInventory, inventoryNetworkBlockEntity, 8, 6);
    }

    public InventoryNetworkScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            @Nullable InventoryNetworkBlockEntity inventoryNetworkBlockEntity,
            int columns,
            int maxRows
    ) {
        super(CWScreenHandlers.INVENTORY_NETWORK, syncId);

        this.columns = columns;
        this.maxRows = maxRows;
        this.player = playerInventory.player;
        this.virtualNetworkInventory = new ScrollVirtualNetworkInventory(this);
        this.inventoryNetworkBlockEntity = inventoryNetworkBlockEntity;

        this.getNetwork().ifPresent(network -> network.addListener(this));

        if (inventoryNetworkBlockEntity != null) {
            inventoryNetworkBlockEntity.onOpen(this.player);
        }

        this.updateSlots(0);
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
        int size = this.isSearching() ? this.searchResults.size() : this.getNetworkSize();
        return MathHelper.ceilDiv(size, this.getColumns()) - this.getMaxRows();
    }

    public int getRow(float scroll) {
        return Math.max((int) (scroll * this.getOverflowRows() + 0.5), 0);
    }

    public float getScrollPosition() {
        return this.scrollPosition;
    }

    public boolean isScrollerEnabled() {
        int maxSlots = this.getColumns() * this.getMaxRows();

        int slots;

        if (this.isSearching()) {
            slots = this.searchResults.size();
        } else {
            slots = this.getNetworkSize();
        }

        return slots > maxSlots;
    }

    public int getPlayerInventorySize() {
        return this.player.getInventory().size();
    }

    public int getVirtualNetworkInventorySize() {
        return this.virtualNetworkInventory.size();
    }

    public VirtualNetworkInventory getVirtualNetworkInventory() {
        return this.virtualNetworkInventory;
    }

    public int getNetworkSize() {
        return this.getNetwork().map(InventoryNetwork::size).orElse(0);
    }

    public ItemStack getNetworkStack(int slotIndex) {
        return this.getNetwork().map(network -> network.getStack(slotIndex)).orElse(ItemStack.EMPTY);
    }

    public void removeNetworkStack(int slotIndex) {
        this.getNetwork()
            .map(network -> network.removeStack(slotIndex));
    }

    public ItemStack removeNetworkStack(int slotIndex, int amount) {
        return this.getNetwork()
                   .map(network -> network.removeStack(slotIndex, amount))
                   .orElse(ItemStack.EMPTY);
    }

    public Optional<InventoryNetwork> getNetwork() {
        if (this.player instanceof InventoryNetworkState networkState) {
            return networkState.copperworks2$getNetwork();
        }

        return Optional.empty();
    }

    public List<SearchEntry> getSearchResults() {
        return List.copyOf(this.searchResults);
    }

    public boolean isSearching() {
        return this.searchQuery != null && !this.searchQuery.isBlank();
    }

    public void updateScrollPosition(float scrollPosition) {
        this.scrollPosition = scrollPosition;
        this.virtualNetworkInventory.scrollItems(scrollPosition);
    }

    public float calculateScrollPosition(float current, double amount) {
        return MathHelper.clamp(current - (float) (amount / this.getOverflowRows()), 0, 1);
    }

    public int virtualToRealIndex(float position, int virtualIndex) {
        int columns = this.getColumns();
        int scrollOffsetRows = this.getRow(position);

        int column = virtualIndex % columns;
        int row = virtualIndex / columns;

        return column + (row + scrollOffsetRows) * columns;
    }

    protected void clearSlots() {
        this.slots.clear();
        this.trackedStacks.clear();
        this.trackedSlots.clear();
    }

    protected void updateSlots(float scrollPosition) {
        this.clearSlots();

        this.getNetwork().ifPresent(InventoryNetwork::markDirty);

        if (this.isSearching()) {
            this.virtualNetworkInventory = new SearchVirtualNetworkInventory(this);
        } else {
            this.virtualNetworkInventory = new ScrollVirtualNetworkInventory(this);
        }

        SlotSupplier slotSupplier = this.virtualNetworkInventory instanceof SearchVirtualNetworkInventory ?
                SearchSlot::new :
                Slot::new;

        for (int y = 0; y < this.getRows(); y++) {
            for (int x = 0; x < this.getColumns(); x++) {
                int index = x + y * this.getColumns();
                int slotX = (SLOT_SIZE / 2) + x * SLOT_BOX_SIZE;
                int slotY = INVENTORY_Y_OFFSET + SLOT_BOX_SIZE + y * SLOT_BOX_SIZE;

                this.addSlot(slotSupplier.apply(this.virtualNetworkInventory, index, slotX, slotY));
            }
        }

        this.updateScrollPosition(scrollPosition);
        this.virtualNetworkInventory.setPrepared();

        this.addPlayerSlots(
                this.player.getInventory(),
                SLOT_SIZE / 2,
                INVENTORY_Y_OFFSET + SLOT_BOX_SIZE + this.getRows() * SLOT_BOX_SIZE + 13
        );

        ScreenHandlerSyncHandler screenHandlerSyncHandler = ((ScreenHandlerAccessor) this).copperworks$getSyncHandler();

        if (screenHandlerSyncHandler != null) {
            this.updateSyncHandler(screenHandlerSyncHandler);
        }
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        super.onSlotClick(slotIndex, button, actionType, player);
        this.getNetwork().ifPresent(InventoryNetwork::markDirty);
    }

    @Override
    public void sendContentUpdates() {
        this.sendNetworkUpdates();
        super.sendContentUpdates();
        this.performServerValidation();
    }

    @Override
    public void syncState() {
        this.sendNetworkUpdates();
        super.syncState();
        this.performServerValidation();
    }

    protected void performServerValidation() {
        if (this.player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(
                    serverPlayer,
                    new InventoryValidationPacket(
                            this.syncId,
                            this.scrollPosition,
                            Optional.ofNullable(this.searchQuery)
                    )
            );
        }
    }

    @Override
    public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
        if (this.isValidated()) {
            for (int index = 0; index < stacks.size(); index++) {
                ItemStack stack = stacks.get(index);
                Slot slot = this.getSlot(index);

                if (interceptSetSlotStack(slot, stack)) {
                    slot.setStackNoCallbacks(stack);
                }
            }

            this.setCursorStack(cursorStack);
            ((ScreenHandlerAccessor) this).copperworks$setRevision(revision);
        } else {
            Copperworks.LOGGER.warn("Not validated in updateSlotStacks");
        }
    }

    @Override
    public void setStackInSlot(int index, int revision, ItemStack stack) {
        if (this.isValidated()) {
            Slot slot = this.getSlot(index);

            if (interceptSetSlotStack(slot, stack)) {
                slot.setStackNoCallbacks(stack);
            }

            ((ScreenHandlerAccessor) this).copperworks$setRevision(revision);
        } else {
            Copperworks.LOGGER.warn("Not validated in setStackInSlot");
        }
    }

    protected boolean interceptSetSlotStack(Slot slot, ItemStack stack) {
        if (slot.inventory instanceof VirtualNetworkInventory slotInventory) {
            slotInventory.setStackNoCallbacks(slot.getIndex(), stack);
            return false;
        }

        return true;
    }

    public void applyServerStacks(List<StackWithSlot> stacks) {
        this.getNetwork().ifPresent(network -> {
            for (StackWithSlot stackWithSlot : stacks) {
                int slotIndex = stackWithSlot.slot();
                ItemStack serverStack = stackWithSlot.stack();
                ItemStack currentStack = network.getStack(slotIndex);

                if (InventoryHelper.didStackChange(currentStack, serverStack)) {
                    break;
                }
            }

            network.clearNoCallbacks();

            for (StackWithSlot stackWithSlot : stacks) {
                network.setStackNoCallbacks(stackWithSlot.slot(), stackWithSlot.stack());
            }
        });
    }

    public boolean isValidated() {
        boolean scrollPositionValid = Objects.equals(this.scrollPosition, this.validatedScrollPosition);
        boolean searchQueryValid = Objects.equals(this.searchQuery, this.validatedSearchQuery);

        return scrollPositionValid && searchQueryValid;
    }

    public boolean handlePickupAll(int clickedSlotIndex, int button, SlotActionType actionType) {
        int networkSize = this.getNetworkSize();
        int virtualNetworkInventorySize = this.getVirtualNetworkInventorySize();
        int playerInventorySize = this.getPlayerInventorySize();
        int slotCount = this.slots.size();

        if (actionType != SlotActionType.PICKUP_ALL || clickedSlotIndex < 0 || clickedSlotIndex >= slotCount) {
            return false;
        }

        Slot clickedSlot = this.slots.get(clickedSlotIndex);
        ItemStack cursorStack = this.getCursorStack();

        if (!cursorStack.isEmpty() && (!clickedSlot.hasStack() || !clickedSlot.canTakeItems(this.player))) {
            int direction = button == 0 ? 1 : -1;

            int startIndex = button == 0 ? 0 : networkSize - 1;

            for (int pass = 0; pass < 2; pass++) {
                for (
                        int index = startIndex;
                        index >= 0 && index < networkSize && cursorStack.getCount() < cursorStack.getMaxCount();
                        index += direction
                ) {
                    ItemStack stack = this.getNetworkStack(index);

                    if (!stack.isEmpty() && InventoryHelper.canCombine(stack, cursorStack, true)) {
                        if (pass != 0 || stack.getCount() != stack.getMaxCount()) {
                            ItemStack takenStack = this.takeStackRange(
                                    index,
                                    stack.getCount(),
                                    cursorStack.getMaxCount() - cursorStack.getCount()
                            );

                            cursorStack.increment(takenStack.getCount());
                        }
                    }
                }
            }

            int playerInventoryStartIndex = button == 0 ? 0 : playerInventorySize - 1;

            for (int pass = 0; pass < 2; pass++) {
                for (
                        int index = playerInventoryStartIndex;
                        index >= 0 && index < playerInventorySize && cursorStack.getCount() < cursorStack.getMaxCount();
                        index += direction
                ) {
                    int adjustedIndex = index + virtualNetworkInventorySize - 1;

                    if (adjustedIndex >= slotCount) {
                        continue;
                    }

                    Slot currentSlot = this.slots.get(adjustedIndex);

                    if (
                            currentSlot.hasStack()
                                    && canInsertItemIntoSlot(currentSlot, cursorStack, true)
                                    && currentSlot.canTakeItems(this.player)
                                    && this.canInsertIntoSlot(cursorStack, currentSlot)
                    ) {
                        ItemStack slotStack = currentSlot.getStack();

                        if (pass != 0 || slotStack.getCount() != slotStack.getMaxCount()) {
                            ItemStack takenStack = currentSlot.takeStackRange(
                                    slotStack.getCount(),
                                    cursorStack.getMaxCount() - cursorStack.getCount(),
                                    player
                            );

                            cursorStack.increment(takenStack.getCount());
                        }
                    }
                }
            }
        }

        return true;
    }

    protected ItemStack takeStackRange(int index, int min, int max) {
        if (max < this.getNetworkStack(index).getCount()) {
            return ItemStack.EMPTY;
        }

        ItemStack itemStack = this.removeNetworkStack(index, Math.min(min, max));

        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (this.getNetworkStack(index).isEmpty()) {
            this.removeNetworkStack(index);
        }

        return itemStack;
    }

    public void quickMoveAll(ItemStack quickMovingStack) {
        this.getNetwork().ifPresent(network -> {
            int virtualNetworkSize = this.getVirtualNetworkInventorySize();
            boolean changed = false;

            for (StackWithSlot stackWithSlot : network.getHeldStacks()) {
                ItemStack stack = stackWithSlot.stack();

                if (!stack.isEmpty() && InventoryHelper.canCombine(stack, quickMovingStack, true)) {
                    if (this.insertItem(stack, virtualNetworkSize, this.slots.size(), true)) {
                        changed = true;
                    }
                }
            }

            if (changed) {
                this.getNetwork().ifPresent(InventoryNetwork::markDirty);
            }
        });
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        int virtualNetworkSize = this.getVirtualNetworkInventorySize();

        ItemStack resultStack;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            resultStack = slotStack.copy();

            if (slotIndex < virtualNetworkSize) {
                if (this.insertItem(slotStack, virtualNetworkSize, this.slots.size(), true)) {
                    // Prevents unintended repeated quick moves when searching
                    if (this.virtualNetworkInventory instanceof SearchVirtualNetworkInventory) {
                        resultStack = ItemStack.EMPTY;
                    }

                    this.getNetwork().ifPresent(InventoryNetwork::markDirty);
                } else {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItemToNetwork(slotStack)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        } else {
            resultStack = ItemStack.EMPTY;
        }

        return resultStack;
    }

    protected boolean insertItemToNetwork(ItemStack stack) {
        return this.getNetwork().map(network -> {
            final int networkSize = network.size();
            boolean changed = false;
            int slotIndex = 0;

            if (stack.isStackable()) {
                while (!stack.isEmpty() && slotIndex < networkSize) {
                    ItemStack itemStack = network.getStack(slotIndex);

                    if (!itemStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, itemStack)) {
                        int combinedCount = itemStack.getCount() + stack.getCount();
                        int slotCapacity = itemStack.getMaxCount();

                        if (combinedCount <= slotCapacity) {
                            stack.setCount(0);
                            itemStack.setCount(combinedCount);
                            changed = true;
                        } else if (itemStack.getCount() < slotCapacity) {
                            stack.decrement(slotCapacity - itemStack.getCount());
                            itemStack.setCount(slotCapacity);
                            changed = true;
                        }
                    }

                    slotIndex++;
                }
            }

            if (!stack.isEmpty()) {
                slotIndex = 0;

                while (slotIndex < networkSize) {
                    ItemStack targetStack = network.getStack(slotIndex);

                    if (targetStack.isEmpty()) {
                        int maxInsert = targetStack.getMaxCount();
                        network.setStackNoCallbacks(slotIndex, stack.split(Math.min(stack.getCount(), maxInsert)));
                        changed = true;
                        break;
                    }

                    slotIndex++;
                }
            }

            if (changed) {
                network.markDirty();
            }

            return changed;
        }).orElse(false);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.getNetwork()
                   .map(network -> network.canPlayerUse(player))
                   .orElse(false);
    }

    @Override
    public void onInventoryChanged(Inventory sender) {
        this.onContentChanged(sender);

        if (this.isSearching()) {
            if (InventoryHelper.didStacksChange(this.searchResultStacks, this.getSearchResultStacks())) {
                this.searchResults.removeIf(result -> this.getNetworkStack(result.getSlot()).isEmpty());
            }
        }

        this.virtualNetworkInventory.scrollItems(this.scrollPosition);
    }

    protected void sendNetworkUpdates() {
        this.getNetwork().ifPresent(network -> {
            if (this.player instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(
                        serverPlayer,
                        new InventoryNetworkUpdatePacket(this.syncId, network.getHeldStacks())
                );
            }
        });
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        this.getNetwork().ifPresent(network -> {
            network.removeListener(this);
            network.markDirty();
        });

        if (player instanceof InventoryNetworkState networkState) {
            networkState.copperworks2$setNetwork(null);
        }

        if (this.inventoryNetworkBlockEntity != null) {
            this.inventoryNetworkBlockEntity.onClose(this.player);
        }
    }

    @Override
    public void onScroll(float scrollPosition) {
        this.updateScrollPosition(scrollPosition);
    }

    @Override
    public void onScrollValidation(float scrollPosition) {
        this.validatedScrollPosition = scrollPosition;
    }

    @Override
    public void onSearchQueryValidation(@Nullable String query) {
        this.validatedSearchQuery = query;
    }

    @Override
    public void updateSearchEntries(List<SearchEntry> entries) {
        this.updateSearchEntries(entries, true, true);
    }

    @Override
    public void updateSearchEntries(List<SearchEntry> entries, boolean clearExisting, boolean updateSlots) {
        if (clearExisting) {
            this.searchEntries.clear();
        }

        this.searchEntries.addAll(entries);
    }

    @Override
    public void updateSearchQuery(@Nullable String query) {
        String previousQuery = this.searchQuery;

        if (query == null) {
            this.searchQuery = null;
        } else {
            String normalized = Formatting.strip(query).trim().toLowerCase(Locale.ROOT);
            this.searchQuery = normalized.isEmpty() ? null : normalized;
        }

        this.search(!Objects.equals(previousQuery, this.searchQuery), 0);
    }

    public void search(boolean queryChanged, float scrollPosition) {
        List<SearchEntry> previousSearchResults = new ArrayList<>(this.searchResults);

        this.searchResults.clear();

        if (this.searchQuery != null) {
            this.searchResults.addAll(
                    this.searchEntries.stream()
                                      .filter(
                                              entry -> entry.getTerms()
                                                            .stream()
                                                            .anyMatch(term -> term.contains(this.searchQuery))
                                      )
                                      .toList()
            );

            this.searchResults.sort(Comparator.comparing(SearchEntry::getSlot));
        }

        boolean searchResultsChanged = !previousSearchResults.equals(this.searchResults);

        if (searchResultsChanged) {
            this.searchResultStacks.clear();
            this.searchResultStacks.addAll(this.getSearchResultStacks());
        }

        if (queryChanged || searchResultsChanged) {
            this.updateSlots(scrollPosition);
        }
    }

    protected List<ItemStack> getSearchResultStacks() {
        return this.searchResults.stream()
                                 .map(result -> this.getNetworkStack(result.getSlot()).copy())
                                 .toList();
    }
}
