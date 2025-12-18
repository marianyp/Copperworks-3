package dev.mariany.copperworks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.copperworks.block.StickyLogic;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin {
    @WrapOperation(
            method = "pushAwayFrom",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;isConnectedThroughVehicle(Lnet/minecraft/entity/Entity;)Z"
            )
    )
    public boolean wrapPushAwayFrom(Entity entity, Entity fromEntity, Operation<Boolean> original) {
        if(StickyLogic.isStuck(entity)) {
            return false;
        }

        return original.call(entity, fromEntity);
    }
}
