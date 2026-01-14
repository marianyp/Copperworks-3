package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.client.gui.screen.ingame.InventoryNetworkScreen;
import dev.mariany.copperworks.inventory.SearchVirtualNetworkInventory;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> {
    @Shadow
    private boolean doubleClicking;

    @Shadow
    @Nullable
    protected abstract Slot getSlotAt(double mouseX, double mouseY);

    @Shadow
    private ItemStack quickMovingStack;

    @Shadow
    @Final
    protected T handler;

    @Inject(method = "mouseReleased", at = @At(value = "HEAD"))
    public void injectMouseReleased(Click click, CallbackInfoReturnable<Boolean> cir) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;

        boolean shiftDoubleClick = this.doubleClicking && click.hasShift();
        Slot slot = this.getSlotAt(click.x(), click.y());

        if (slot == null || click.button() != 0 || !shiftDoubleClick) {
            return;
        }

        // Prevents unintended repeated quick moves when searching
        if (slot.inventory instanceof SearchVirtualNetworkInventory) {
            this.doubleClicking = false;
            return;
        }

        if (this.handler.canInsertIntoSlot(ItemStack.EMPTY, slot) && !this.quickMovingStack.isEmpty()) {
            if (screen instanceof InventoryNetworkScreen inventoryNetworkScreen) {
                inventoryNetworkScreen.handleQuickMoveAll(this.quickMovingStack);
            }
        }
    }
}
