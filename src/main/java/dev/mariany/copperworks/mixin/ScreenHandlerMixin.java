package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Inject(method = "internalOnSlotClick", at = @At(value = "HEAD"), cancellable = true)
    private void injectInternalOnSlotClick(
            int slotIndex,
            int button,
            SlotActionType actionType,
            PlayerEntity player,
            CallbackInfo ci
    ) {
        ScreenHandler handler = (ScreenHandler) (Object) this;

        if (handler instanceof InventoryNetworkScreenHandler inventoryNetworkScreenHandler) {
            if(inventoryNetworkScreenHandler.handlePickupAll(slotIndex, button, actionType)) {
                ci.cancel();
            }
        }
    }
}
