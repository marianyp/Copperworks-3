package dev.mariany.copperworks.item;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.component.FlyingEquippableComponent;
import dev.mariany.copperworks.item.custom.PatinaItem;
import dev.mariany.copperworks.item.custom.RocketBootsItem;
import dev.mariany.copperworks.item.custom.WrenchItem;
import dev.mariany.copperworks.item.custom.copperupgrade.CopperUpgradeItem;
import dev.mariany.copperworks.item.custom.radio.RadioItem;
import dev.mariany.copperworks.item.equipment.CWArmorMaterials;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

import java.util.function.Function;

public class CWItems {
    public static final Item COPPER_PLATE = register("copper_plate");

    public static final Item IRON_PLATE = register("iron_plate");

    public static final Item AMETHYST_PIECE = register("amethyst_piece");

    public static final CopperUpgradeItem COPPER_UPGRADE_KIT = register(
            "copper_upgrade_kit",
            CopperUpgradeItem::new,
            new Item.Settings().rarity(Rarity.UNCOMMON).maxDamage(64).repairable(Items.COPPER_INGOT)
    );

    public static final RadioItem RADIO = register("radio", RadioItem::new, new Item.Settings().maxCount(1));

    public static final Item ROCKET_BOOTS = register(
            "rocket_boots",
            RocketBootsItem::new,
            createRocketBootsSettings()
    );

    public static final PatinaItem PATINA = register("patina", PatinaItem::new);

    public static final WrenchItem WRENCH = register(
            "wrench",
            WrenchItem::new,
            new Item.Settings().maxDamage(256).repairable(Items.COPPER_INGOT)
    );

    private static Item.Settings createRocketBootsSettings() {
        ArmorMaterial material = CWArmorMaterials.ROCKET_BOOTS;

        return new Item.Settings()
                .fireproof()
                .maxDamage(2400)
                .repairable(material.repairIngredient())
                .enchantable(material.enchantmentValue())
                .attributeModifiers(material.createAttributeModifiers(EquipmentType.BOOTS))
                .component(
                        DataComponentTypes.EQUIPPABLE,
                        EquippableComponent.builder(EquipmentSlot.FEET)
                                           .equipSound(material.equipSound())
                                           .model(material.assetId())
                                           .damageOnHurt(false)
                                           .build()
                )
                .component(
                        CWComponents.FLYING_EQUIPPABLE,
                        new FlyingEquippableComponent(
                                ParticleTypes.FLAME,
                                1.8F,
                                0.06F,
                                20
                        )
                );
    }

    private static Item register(String name) {
        return register(name, Item::new, new Item.Settings());
    }

    private static <T extends Item> T register(String name, Function<Item.Settings, T> factory) {
        return register(name, factory, new Item.Settings());
    }

    private static <T extends Item> T register(
            String name,
            Function<Item.Settings, T> factory,
            Item.Settings settings
    ) {
        RegistryKey<Item> itemKey = keyOf(name);
        T item = factory.apply(settings.registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }

    private static RegistryKey<Item> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.ITEM, Copperworks.id(id));
    }

    public static void bootstrap() {
        Copperworks.bootstrapLog("Items");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.addAfter(Items.IRON_INGOT, IRON_PLATE);
            entries.addAfter(Items.COPPER_INGOT, COPPER_PLATE);

            entries.addAfter(Items.GUNPOWDER, PATINA);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.addBefore(Items.NAME_TAG, COPPER_UPGRADE_KIT);

            entries.addBefore(Items.COMPASS, RADIO);

            entries.addAfter(Items.ELYTRA, ROCKET_BOOTS);

            entries.addBefore(Items.BRUSH, WRENCH);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE)
                       .register(entries -> entries.addBefore(Items.LEVER, RADIO));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                       .register(entries -> entries.addAfter(Items.NETHERITE_BOOTS, ROCKET_BOOTS));
    }
}
