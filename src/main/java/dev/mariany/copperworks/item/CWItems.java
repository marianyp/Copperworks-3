package dev.mariany.copperworks.item;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.component.FlyingEquippableComponent;
import dev.mariany.copperworks.item.custom.PartialDragonBreathItem;
import dev.mariany.copperworks.item.custom.RocketBootsItem;
import dev.mariany.copperworks.item.custom.copperupgrade.CopperUpgradeItem;
import dev.mariany.copperworks.item.custom.radio.RadioItem;
import dev.mariany.copperworks.item.equipment.CWArmorMaterials;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.particle.DragonBreathParticleEffect;
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

    public static final PartialDragonBreathItem PARTIAL_DRAGON_BREATH = register(
            "partial_dragon_breath",
            settings -> new PartialDragonBreathItem(settings, 3),
            new Item.Settings()
                    .rarity(Rarity.UNCOMMON)
                    .component(CWComponents.DRAGON_BREATH_FULLNESS, 1)
    );

    public static final Item ENDER_POWDER = register("ender_powder");

    public static final CopperUpgradeItem COPPER_UPGRADE_KIT = register(
            "copper_upgrade_kit",
            CopperUpgradeItem::new,
            new Item.Settings().rarity(Rarity.UNCOMMON).maxDamage(64).repairable(Items.COPPER_INGOT)
    );

    public static final RadioItem RADIO = register("radio", RadioItem::new, new Item.Settings().maxCount(1));

    public static final Item ROCKET_BOOTS = register(
            "rocket_boots",
            RocketBootsItem::new,
            new Item.Settings()
                    .armor(CWArmorMaterials.ROCKET_BOOTS, EquipmentType.BOOTS)
                    .maxDamage(720)
                    .fireproof()
                    .component(
                            CWComponents.FLYING_EQUIPPABLE,
                            new FlyingEquippableComponent(
                                    DragonBreathParticleEffect.of(ParticleTypes.DRAGON_BREATH, 4),
                                    1.8F,
                                    0.06F,
                                    20
                            )
                    )
    );

    private static Item register(String name) {
        return register(name, Item::new, new Item.Settings());
    }

    private static Item register(String name, Item.Settings settings) {
        return register(name, Item::new, settings);
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
        Copperworks.LOGGER.info("Registering Items for {}", Copperworks.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            partialDragonBreathItemGroup(entries);

            entries.addAfter(Items.IRON_INGOT, IRON_PLATE);
            entries.addAfter(Items.COPPER_INGOT, COPPER_PLATE);
            entries.addAfter(Items.BLAZE_POWDER, ENDER_POWDER);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.addBefore(Items.NAME_TAG, COPPER_UPGRADE_KIT);
            entries.addBefore(Items.COMPASS, RADIO);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                       .register(entries -> entries.addAfter(Items.NETHERITE_BOOTS, ROCKET_BOOTS));
    }

    private static void partialDragonBreathItemGroup(FabricItemGroupEntries entries) {
        for (int i = 1; i < PARTIAL_DRAGON_BREATH.getMaxFullness() + 1; i++) {
            ItemStack itemStack = PARTIAL_DRAGON_BREATH.getDefaultStack();
            itemStack.set(CWComponents.DRAGON_BREATH_FULLNESS, i);
            entries.addBefore(Items.DRAGON_BREATH, itemStack);
        }
    }
}
