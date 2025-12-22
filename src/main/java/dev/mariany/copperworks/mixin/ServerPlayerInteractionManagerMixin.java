package dev.mariany.copperworks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.mariany.copperworks.item.upgrade.copper.CopperUpgradeItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @WrapOperation(
            method = "interactBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;shouldCancelInteraction()Z"
            )
    )
    public boolean wrapInteractBlock(
            ServerPlayerEntity player,
            Operation<Boolean> original,
            @Local(index = 4, argsOnly = true) Hand hand,
            @Local(index = 5, argsOnly = true) BlockHitResult hitResult
    ) {
        if (CopperUpgradeItem.shouldOverrideInteraction(player, hand, hitResult)) {
            return true;
        }

        return original.call(player);
    }
}
