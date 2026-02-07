package dev.mariany.copperworks.item.custom;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class RocketBootsItem extends Item {
    public RocketBootsItem(Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributeModifiers() {
        Identifier identifier = Identifier.ofVanilla("armor.boots");
        AttributeModifierSlot attributeModifierSlot = AttributeModifierSlot.FEET;

        return AttributeModifiersComponent
                .builder()
                .add(
                        EntityAttributes.ARMOR,
                        new EntityAttributeModifier(
                                identifier,
                                3,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        attributeModifierSlot
                )
                .add(
                        EntityAttributes.ARMOR_TOUGHNESS,
                        new EntityAttributeModifier(
                                identifier,
                                3,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        attributeModifierSlot
                )
                .add(
                        EntityAttributes.KNOCKBACK_RESISTANCE,
                        new EntityAttributeModifier(
                                identifier,
                                0.1,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        attributeModifierSlot
                )
                .build();
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        if (stack.willBreakNextUse()) {
            return 0;
        }

        return MathHelper.clamp(Math.round(13F - stack.getDamage() * 13F / stack.getMaxDamage()), 1, 13);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendTooltip(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplayComponent displayComponent,
            Consumer<Text> textConsumer,
            TooltipType type
    ) {
        if (stack.willBreakNextUse()) {
            textConsumer.accept(
                    Text.translatable("item.copperworks.rocket_boots.out_of_fuel").formatted(Formatting.GRAY)
            );
        }
    }
}
