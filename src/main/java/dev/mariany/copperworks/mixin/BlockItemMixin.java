package dev.mariany.copperworks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.copperworks.item.custom.AlternativeScaffoldingBlockItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    /**
     * Patch to fix AlternativeScaffoldingBlock placement sound not playing on the client for the block placer
     */
    @WrapOperation(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"
            )
    )
    public void wrapPlace(
            World world,
            Entity source,
            BlockPos pos,
            SoundEvent sound,
            SoundCategory category,
            float volume,
            float pitch,
            Operation<Void> original
    ) {
        BlockItem blockItem = (BlockItem) (Object) this;

        if (blockItem instanceof AlternativeScaffoldingBlockItem) {
            source = null;
        }

        original.call(world, source, pos, sound, category, volume, pitch);
    }
}
