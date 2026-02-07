package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.component.FlyingEquippableComponent;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "applyAttributeModifier", at = @At(value = "HEAD"), cancellable = true)
    protected void injectApplyAttributeModifier(
            AttributeModifierSlot slot,
            TriConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier, AttributeModifiersComponent.Display> attributeModifierConsumer,
            CallbackInfo ci
    ) {
        ItemStack stack = (ItemStack) (Object) this;

        if (FlyingEquippableComponent.areAttributeModifiersDisabled(stack)) {
            ci.cancel();
        }
    }

    @Inject(method = "applyAttributeModifiers", at = @At(value = "HEAD"), cancellable = true)
    protected void injectApplyAttributeModifiers(
            EquipmentSlot slot,
            BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer,
            CallbackInfo ci
    ) {
        ItemStack stack = (ItemStack) (Object) this;

        if (FlyingEquippableComponent.areAttributeModifiersDisabled(stack)) {
            ci.cancel();
        }
    }
}
