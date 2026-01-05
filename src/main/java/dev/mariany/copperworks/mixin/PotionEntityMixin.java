package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.event.entity.EntityEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotionEntity.class)
public class PotionEntityMixin {
    @Inject(method = "onCollision", at = @At(value = "HEAD"))
    protected void injectOnCollision(HitResult hitResult, CallbackInfo ci) {
        PotionEntity potionEntity = (PotionEntity) (Object) this;

        if (potionEntity.getEntityWorld() instanceof ServerWorld world) {
            ItemStack itemStack = potionEntity.getStack();

            PotionContentsComponent potionContentsComponent = itemStack.getOrDefault(
                    DataComponentTypes.POTION_CONTENTS,
                    PotionContentsComponent.DEFAULT
            );

            EntityEvents.BEFORE_POTION_COLLISION
                    .invoker()
                    .onPotionCollision(world, hitResult.getPos(), potionContentsComponent, 3F);
        }
    }
}
