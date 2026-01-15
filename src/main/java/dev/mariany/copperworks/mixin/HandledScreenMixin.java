package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.client.gui.screen.ingame.InventoryNetworkScreen;
import dev.mariany.copperworks.inventory.SearchVirtualInventoryNetwork;
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

        Slot slot = this.getSlotAt(click.x(), click.y());

        if (slot == null) {
            return;
        }

        if (slot.inventory instanceof SearchVirtualInventoryNetwork) {
            // Prevents unintended quick moves when searching inventory
            this.doubleClicking = false;
            return;
        }

        if (click.button() != 0 || !this.doubleClicking || !click.hasShift()) {
            return;
        }

        if (!this.quickMovingStack.isEmpty() && this.handler.canInsertIntoSlot(ItemStack.EMPTY, slot)) {
            if (screen instanceof InventoryNetworkScreen inventoryNetworkScreen) {
                inventoryNetworkScreen.handleQuickMoveAll(this.quickMovingStack);
            }
        }
    }
}
