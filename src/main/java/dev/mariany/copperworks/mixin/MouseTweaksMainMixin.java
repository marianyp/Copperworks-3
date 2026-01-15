package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.client.gui.screen.ingame.InventoryNetworkScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevent MouseTweak mouse scrolling in InventoryNetworkScreen to prevent weird interactions while scrolling the GUI
 */
@Pseudo
@Mixin(targets = {"yalter.mousetweaks.Main"})
public class MouseTweaksMainMixin {
    @Inject(method = "onMouseScrolled", at = @At(value = "HEAD"), cancellable = true)
    private static void copperworks$injectionMouseScrolled(
            Screen screen,
            double x,
            double y,
            double scrollDelta,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (screen instanceof InventoryNetworkScreen) {
            cir.setReturnValue(false);
        }
    }
}
