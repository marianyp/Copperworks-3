package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.component.FlyingEquippableComponent;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At(value = "TAIL")
    )
    public <T extends LivingEntity, S extends LivingEntityRenderState> void injectUpdateRenderState(
            T livingEntity,
            S state,
            float tickProgress,
            CallbackInfo ci
    ) {
        if (FlyingEquippableComponent.canFly(livingEntity)) {
            if (FlyingEquippableComponent.shouldShowParticles(livingEntity)) {
                float limbSwingAmplitude = state.limbSwingAmplitude;
                state.limbSwingAmplitude = limbSwingAmplitude > 0 ? limbSwingAmplitude / 4 : 0;
            }
        }
    }
}
