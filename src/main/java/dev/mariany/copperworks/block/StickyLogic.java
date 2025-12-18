package dev.mariany.copperworks.block;

import com.google.common.collect.HashMultimap;
import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.tag.CWTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;

public interface StickyLogic {
    static boolean isStuck(Entity entity) {
        if (entity.getType().isIn(CWTags.Entities.STICKY_IMMUNE)) {
            return false;
        }

        if (entity instanceof PlayerEntity player) {
            if (player.isSneaking() || player.isSpectator() || player.getAbilities().allowFlying) {
                return false;
            }
        }

        return entity.getSteppingBlockState().isIn(CWTags.Blocks.STICKY);
    }

    static void applyModifiers(LivingEntity livingEntity) {
        AttributeContainer attributeContainer = livingEntity.getAttributes();
        HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiers = getModifiers();

        if (isStuck(livingEntity)) {
            attributeContainer.addTemporaryModifiers(modifiers);
        } else {
            attributeContainer.removeModifiers(modifiers);
        }
    }

    private static HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers() {
        EntityAttributeModifier attributeModifier = new EntityAttributeModifier(
                Copperworks.id("sticky_block"),
                1,
                EntityAttributeModifier.Operation.ADD_VALUE
        );

        HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> hashMultimap = HashMultimap.create();
        hashMultimap.put(EntityAttributes.KNOCKBACK_RESISTANCE, attributeModifier);

        return hashMultimap;
    }
}
