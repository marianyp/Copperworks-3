package dev.mariany.copperworks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.mariany.copperworks.block.SelfInserting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Consumer;

@Mixin(Block.class)
public class BlockMixin {
    @WrapOperation(
            method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"
            )
    )
    private static void wrapDropStacks(
            List<ItemStack> stacks,
            Consumer<?> consumer,
            Operation<Void> original,
            @Local(index = 0, argsOnly = true) BlockState state,
            @Local(index = 1, argsOnly = true) World world,
            @Local(index = 2, argsOnly = true) BlockPos pos,
            @Local(index = 3, argsOnly = true) @Nullable BlockEntity blockEntity,
            @Local(index = 4, argsOnly = true) @Nullable Entity entity,
            @Local(index = 5, argsOnly = true) ItemStack tool
    ) {
        if (entity instanceof PlayerEntity player) {
            if (state.getBlock() instanceof SelfInserting) {
                SelfInserting.insert(world, player, pos, stacks);
                return;
            }
        }

        original.call(stacks, consumer);
    }
}
