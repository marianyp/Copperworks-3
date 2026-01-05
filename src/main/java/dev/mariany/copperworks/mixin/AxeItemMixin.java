package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.item.custom.PatinaItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin {
    @Inject(method = "tryStrip", at = @At(value = "HEAD"))
    private void injectTryStrip(
            World world,
            BlockPos pos,
            @Nullable PlayerEntity player,
            BlockState state,
            CallbackInfoReturnable<Optional<BlockState>> cir
    ) {
        PatinaItem.tryStrip(world, pos, state);
    }
}
