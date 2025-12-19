package dev.mariany.copperworks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.mariany.copperworks.item.upgrade.copper.CopperUpgradeItem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @WrapOperation(
            method = "interactBlockInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldCancelInteraction()Z"
            )
    )
    public boolean wrapInteractBlock(
            ClientPlayerEntity player,
            Operation<Boolean> original,
            @Local(index = 2, argsOnly = true) Hand hand
    ) {
        if (player.getStackInHand(hand).getItem() instanceof CopperUpgradeItem) {
            return true;
        }

        return original.call(player);
    }
}
