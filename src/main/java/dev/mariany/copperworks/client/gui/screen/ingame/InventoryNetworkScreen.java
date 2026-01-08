package dev.mariany.copperworks.client.gui.screen.ingame;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.client.gui.widget.ClickableTextFieldWidget;
import dev.mariany.copperworks.packet.serverbound.InventoryScrollPacket;
import dev.mariany.copperworks.packet.serverbound.QuickMoveAllPacket;
import dev.mariany.copperworks.packet.serverbound.UpdateSearchEntriesPacket;
import dev.mariany.copperworks.packet.serverbound.UpdateSearchQueryPacket;
import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import dev.mariany.copperworks.screen.search.SearchEntry;
import dev.mariany.copperworks.screen.slot.SearchSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class InventoryNetworkScreen extends HandledScreen<InventoryNetworkScreenHandler> implements
        InventoryChangedListener {
    protected static final Identifier BACKGROUND_TEXTURE = Copperworks.id(
            "textures/gui/container/inventory_network.png"
    );
    protected static final Identifier SCROLLER_TEXTURE = Copperworks.id(
            "container/inventory_network/scroller"
    );
    protected static final Identifier SCROLLER_DISABLED_TEXTURE = Copperworks.id(
            "container/inventory_network/scroller_disabled"
    );

    protected static final Identifier EMPTY_SEARCH_SLOT_TEXTURE = Copperworks.id(
            "container/inventory_network/empty_search_slot"
    );

    protected static final int TEXTURE_WIDTH = 256;
    protected static final int TEXTURE_HEIGHT = 256;

    protected static final int SCROLL_HANDLE_WIDTH = 12;
    protected static final int SCROLL_HANDLE_HEIGHT = 15;

    protected static final int SCROLL_TRACK_TOP_LEFT_X = 156;
    protected static final int SCROLL_TRACK_TOP_LEFT_Y = 31;
    protected static final int SCROLL_TRACK_HEIGHT = 106;

    protected ClickableTextFieldWidget searchBox;

    protected boolean scrolling;
    protected boolean ignoreTypedCharacter;

    public InventoryNetworkScreen(
            InventoryNetworkScreenHandler handler,
            PlayerInventory inventory,
            Text title
    ) {
        super(handler, inventory, title);

        int yOffset = InventoryNetworkScreenHandler.INVENTORY_Y_OFFSET + 114;

        this.backgroundHeight = yOffset + this.getRows() * InventoryNetworkScreenHandler.SLOT_BOX_SIZE;
        this.titleY += InventoryNetworkScreenHandler.INVENTORY_Y_OFFSET;
        this.playerInventoryTitleY = this.backgroundHeight - 94;

        handler.getNetwork().ifPresent(network -> network.addListener(this));
    }

    protected int getCenterX() {
        return (this.width - this.backgroundWidth) / 2;
    }

    protected int getCenterY() {
        return (this.height - this.backgroundHeight) / 2;
    }

    protected int getRows() {
        return this.handler.getRows();
    }

    protected boolean isScrollerEnabled() {
        return this.handler.isScrollerEnabled();
    }

    protected float getScrollPosition() {
        return this.handler.getScrollPosition();
    }

    @Override
    protected void init() {
        super.init();

        this.updateSearchEntries();

        this.searchBox = new ClickableTextFieldWidget(
                this.textRenderer,
                this.x + 83,
                this.y + 6,
                80,
                9,
                Text.translatable("itemGroup.search")
        );

        this.searchBox.setMaxLength(50);
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setEditableColor(-1);

        this.addSelectableChild(this.searchBox);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        super.drawForeground(context, mouseX, mouseY);

        for (Slot slot : this.handler.slots) {
            if (slot instanceof SearchSlot searchSlot && !searchSlot.isEnabled()) {
                this.drawEmptySearchSlot(context, searchSlot);
            }
        }
    }

    private void drawEmptySearchSlot(DrawContext context, SearchSlot slot) {
        context.drawGuiTexture(
                RenderPipelines.GUI_TEXTURED,
                EMPTY_SEARCH_SLOT_TEXTURE,
                slot.x - 1,
                slot.y - 1,
                InventoryNetworkScreenHandler.SLOT_BOX_SIZE,
                InventoryNetworkScreenHandler.SLOT_BOX_SIZE
        );
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int yOffset = InventoryNetworkScreenHandler.INVENTORY_Y_OFFSET + 17;
        int baseY = this.getRows() * InventoryNetworkScreenHandler.SLOT_BOX_SIZE + yOffset;

        this.drawContainerSlots(context, baseY);
        this.drawPlayerSlots(context, baseY);
        this.drawScrollbar(context, baseY);

        this.searchBox.render(context, mouseX, mouseY, deltaTicks);
    }

    protected void drawContainerSlots(DrawContext context, int baseY) {
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND_TEXTURE,
                this.getCenterX(),
                this.getCenterY(),
                0,
                0,
                this.backgroundWidth,
                baseY,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    protected void drawPlayerSlots(DrawContext context, int baseY) {
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND_TEXTURE,
                this.getCenterX(),
                this.getCenterY() + baseY,
                0,
                139,
                this.backgroundWidth,
                96,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    protected void drawScrollbar(DrawContext context, int baseY) {
        Identifier texture = this.isScrollerEnabled() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;

        int trackTopY = this.y + SCROLL_TRACK_TOP_LEFT_Y;

        int travelDistance = SCROLL_TRACK_HEIGHT - SCROLL_HANDLE_HEIGHT;
        int thumbOffsetY = (int) (travelDistance * this.getScrollPosition());

        int x = this.x + SCROLL_TRACK_TOP_LEFT_X;
        int y = trackTopY + thumbOffsetY;

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND_TEXTURE,
                x - 1,
                this.getCenterY() + baseY - 1,
                155,
                137,
                SCROLL_HANDLE_WIDTH + 2,
                1,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, SCROLL_HANDLE_WIDTH, SCROLL_HANDLE_HEIGHT);
    }

    protected void scroll(float scrollPosition) {
        this.handler.updateScrollPosition(scrollPosition);
        ClientPlayNetworking.send(new InventoryScrollPacket(this.handler.syncId, scrollPosition));
    }

    protected boolean isClickInScrollbar(Click click) {
        double mouseX = click.x();
        double mouseY = click.y();

        int x = this.x;
        int y = this.y;

        int left = x + SCROLL_TRACK_TOP_LEFT_X;
        int right = left + SCROLL_HANDLE_WIDTH + 2;
        int top = y + SCROLL_TRACK_TOP_LEFT_Y;
        int bottom = top + SCROLL_TRACK_HEIGHT + 2;

        return mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        if (this.isScrollerEnabled()) {
            this.scroll(this.handler.calculateScrollPosition(this.handler.getScrollPosition(), verticalAmount));
        }

        return true;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        this.searchBox.setFocused(false);

        if (click.button() == 0) {
            if (this.isClickInScrollbar(click)) {
                this.scrolling = this.isScrollerEnabled();
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (this.scrolling) {
            double mouseY = click.y();
            int trackTopY = this.y + SCROLL_TRACK_TOP_LEFT_Y;
            int trackBottomY = trackTopY + SCROLL_TRACK_HEIGHT + 2;

            float scrollPosition = MathHelper.clamp(
                    ((float) mouseY - trackTopY - ((float) SCROLL_HANDLE_HEIGHT / 2)) /
                            (trackBottomY - trackTopY - SCROLL_HANDLE_HEIGHT),
                    0,
                    1
            );

            this.handler.updateScrollPosition(scrollPosition);
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 && this.scrolling) {
            this.scrolling = false;
            this.scroll(this.getScrollPosition());
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (this.ignoreTypedCharacter) {
            return false;
        }

        String previousSearch = this.searchBox.getText();

        if (this.searchBox.charTyped(input)) {
            if (!Objects.equals(previousSearch, this.searchBox.getText())) {
                this.search();
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        this.ignoreTypedCharacter = false;

        int key = input.key();
        boolean focusingInventorySlot = this.isFocusingInventorySlot();
        boolean pressedNumber = InputUtil.fromKeyCode(input).toInt().isPresent();

        if (focusingInventorySlot && pressedNumber && this.handleHotbarKeyPressed(input)) {
            this.ignoreTypedCharacter = true;
            return true;
        }

        String previousSearch = this.searchBox.getText();

        if (this.searchBox.keyPressed(input)) {
            if (!Objects.equals(previousSearch, this.searchBox.getText())) {
                this.search();
            }

            return true;
        }

        return this.searchBox.isFocused() && this.searchBox.isVisible() && key != GLFW.GLFW_KEY_ESCAPE ||
                super.keyPressed(input);
    }

    protected boolean isFocusingInventorySlot() {
        return !this.isInventorySlot(this.focusedSlot) || (this.focusedSlot != null && this.focusedSlot.hasStack());
    }

    protected boolean isInventorySlot(@Nullable Slot slot) {
        return slot != null && slot.inventory == this.handler.getVirtualNetworkInventory();
    }

    protected void search() {
        String search = this.searchBox.getText();
        ClientPlayNetworking.send(new UpdateSearchQueryPacket(this.handler.syncId, search));
        this.handler.updateSearchQuery(search);
    }

    protected void updateSearchEntries() {
        this.handler.getNetwork().ifPresent(network -> {
            World world = this.client == null ? null : this.client.world;

            List<SearchEntry> searchEntries = SearchEntry.getEntries(world, network.getHeldStacks());

            ClientPlayNetworking.send(new UpdateSearchEntriesPacket(this.handler.syncId, searchEntries));

            this.handler.updateSearchEntries(searchEntries);
        });
    }

    @Override
    public void onInventoryChanged(Inventory sender) {
        this.updateSearchEntries();
    }

    @Override
    public void close() {
        super.close();

        this.handler.getNetwork().ifPresent(network -> network.removeListener(this));
    }

    public void handleQuickMoveAll(ItemStack quickMovingStack) {
        ClientPlayNetworking.send(new QuickMoveAllPacket(this.handler.syncId, quickMovingStack));
        this.handler.quickMoveAll(quickMovingStack);
    }
}
