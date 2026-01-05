package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.block.custom.relay.ender.EnderRelayTracker;
import net.minecraft.server.network.PrepareSpawnTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrepareSpawnTask.PlayerSpawn.class)
public class PrepareSpawnTaskMixin {
    @Inject(
            method = "method_72303",
            at = @At(value = "HEAD")
    )
    private static void injectOnReady(ServerPlayerEntity player, ReadView view, CallbackInfo ci) {
        EnderRelayTracker.readEnderRelays(view, player);
    }
}
