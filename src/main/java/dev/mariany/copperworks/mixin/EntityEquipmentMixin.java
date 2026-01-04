package dev.mariany.copperworks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.component.FlyingEquippableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityEquipment.class)
public class EntityEquipmentMixin {
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;inventoryTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/EquipmentSlot;)V"
            )
    )
    public void injectTick(
            ItemStack stack,
            World world,
            Entity entity,
            EquipmentSlot slot,
            Operation<Void> original
    ) {
        original.call(stack, world, entity, slot);

        FlyingEquippableComponent flyingEquippableComponent = stack.get(CWComponents.FLYING_EQUIPPABLE);

        if (flyingEquippableComponent != null) {
            flyingEquippableComponent.inventoryTick(entity, stack, slot);
        }
    }
}
