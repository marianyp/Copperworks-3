package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.client.muffler.MufflerHandler;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.sound.Source;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {
    @Inject(
            method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectPlay(SoundInstance soundInstance, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        if (MufflerHandler.shouldMuffle(soundInstance)) {
            cir.setReturnValue(SoundSystem.PlayResult.NOT_STARTED);
        }
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V", at = @At("HEAD"), cancellable = true)
    private void injectPlayWithDelay(SoundInstance soundInstance, int delay, CallbackInfo ci) {
        if (MufflerHandler.shouldMuffle(soundInstance)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "method_19748",
            at = @At(value = "TAIL")
    )
    private static void injectTick(float volume, float pitch, Vec3d soundPos, Source source, CallbackInfo ci) {
        if (MufflerHandler.shouldMuffle(soundPos)) {
            source.setVolume(0);
        }
    }
}
