package dev.mariany.copperworks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.copperworks.block.StickyHandler;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.component.FlyingEquippableComponent;
import dev.mariany.copperworks.component.FlyingEquippableStateComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "isImmobile", at = @At(value = "HEAD"), cancellable = true)
    protected void injectIsImmobile(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (livingEntity instanceof PlayerEntity && StickyHandler.isStuck(livingEntity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "onEquipStack", at = @At(value = "HEAD"))
    public void injectOnEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        FlyingEquippableStateComponent newFlyingEquippableStateComponent = newStack.get(
                CWComponents.FLYING_EQUIPPABLE_STATE
        );

        if (oldStack.contains(CWComponents.FLYING_EQUIPPABLE)) {
            FlyingEquippableComponent.onRemoveStack(livingEntity, slot, newFlyingEquippableStateComponent);
        }
    }

    @WrapOperation(
            method = "checkGlidingCollision",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;serverDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"
            )
    )
    private void wrapCheckGlidingCollision(
            LivingEntity livingEntity,
            DamageSource damageSource,
            float damage,
            Operation<Void> original
    ) {
        if (!FlyingEquippableStateComponent.canFly(livingEntity)) {
            original.call(livingEntity, damageSource, damage);
        }
    }
}
