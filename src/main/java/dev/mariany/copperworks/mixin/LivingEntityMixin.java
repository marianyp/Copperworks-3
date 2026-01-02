package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.block.StickyLogic;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "isImmobile", at = @At(value = "HEAD"), cancellable = true)
    protected void injectIsImmobile(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (livingEntity instanceof PlayerEntity &&  StickyLogic.isStuck(livingEntity)) {
            cir.setReturnValue(true);
        }
    }
}
