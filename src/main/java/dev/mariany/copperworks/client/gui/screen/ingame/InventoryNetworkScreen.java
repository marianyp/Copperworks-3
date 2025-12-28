package dev.mariany.copperworks.client.gui.screen.ingame;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.packet.serverbound.InventoryScrollPacket;
import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class InventoryNetworkScreen extends HandledScreen<InventoryNetworkScreenHandler> {
    protected static final Identifier BACKGROUND_TEXTURE = Copperworks.id(
            "textures/gui/container/inventory_network.png"
    );
    protected static final Identifier SCROLLER_TEXTURE = Copperworks.id(
            "container/inventory_network/scroller"
    );
    protected static final Identifier SCROLLER_DISABLED_TEXTURE = Copperworks.id(
            "container/inventory_network/scroller_disabled"
    );

    protected static final int TEXTURE_WIDTH = 256;
    protected static final int TEXTURE_HEIGHT = 256;

    protected static final int SCROLL_HANDLE_WIDTH = 12;
    protected static final int SCROLL_HANDLE_HEIGHT = 15;

    protected static final int SCROLL_TRACK_TOP_LEFT_X = 156;
    protected static final int SCROLL_TRACK_TOP_LEFT_Y = 31;
    protected static final int SCROLL_TRACK_HEIGHT = 106;

    protected TextFieldWidget searchBox;

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

        this.searchBox = new TextFieldWidget(
                this.textRenderer,
                this.x + 83,
                this.y + 6,
                80,
                9,
                Text.translatable("itemGroup.search")
        );

        this.searchBox.setMaxLength(50);
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setVisible(true);
        this.searchBox.setEditableColor(-1);
        this.searchBox.setFocused(true);

        this.addSelectableChild(this.searchBox);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
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

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.isClickInScrollbar(mouseX, mouseY)) {
                this.scrolling = this.isScrollerEnabled();
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
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

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.scrolling) {
            this.scrolling = false;
            this.scroll(this.getScrollPosition());
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (this.ignoreTypedCharacter) {
            return false;
        }

        String previousSearch = this.searchBox.getText();

        Copperworks.LOGGER.info(this.searchBox.getText());

        if (this.searchBox.charTyped(character, modifiers)) {
            Copperworks.LOGGER.info(this.searchBox.getText());

            if (!Objects.equals(previousSearch, this.searchBox.getText())) {
//                this.search();
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;

        boolean focusingInventorySlot = !this.isInventorySlot(this.focusedSlot) || this.focusedSlot.hasStack();
        boolean pressedNumber = InputUtil.fromKeyCode(keyCode, scanCode).toInt().isPresent();

        if (focusingInventorySlot && pressedNumber && this.handleHotbarKeyPressed(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            return true;
        }

        String previousSearch = this.searchBox.getText();

//        Copperworks.LOGGER.info("isVisible: {}", this.searchBox.isVisible());
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            Copperworks.LOGGER.info(this.searchBox.getText());

            if (!Objects.equals(previousSearch, this.searchBox.getText())) {
//                this.search();
            }

            return true;
        }

        return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE ||
                super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean isInventorySlot(@Nullable Slot slot) {
        return slot != null && slot.inventory == this.handler.getVirtualInventory();
    }
}
