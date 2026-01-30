package dev.mariany.copperworks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.copperworks.tag.CWTags;
import net.minecraft.block.entity.PistonBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonBlockEntity.class)
public class PistonBlockEntityMixin {
    @WrapOperation(
            method = "moveEntitiesInHoneyBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/PistonBlockEntity;isPushingHoneyBlock()Z")
    )
    private static boolean wrapMoveEntitiesInHoneyBlock(
            PistonBlockEntity pistonBlockEntity,
            Operation<Boolean> original
    ) {
        if (pistonBlockEntity.getPushedBlock().isIn(CWTags.Blocks.STICKY)) {
            return true;
        }

        return original.call(pistonBlockEntity);
    }
}
