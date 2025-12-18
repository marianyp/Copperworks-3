package dev.mariany.copperworks.item;

import dev.mariany.copperworks.Copperworks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

import java.util.function.Function;

public class CWItems {
    public static final Item COPPER_PLATE = register("copper_plate");

    public static final Item IRON_PLATE = register("iron_plate");

    public static final Item COPPER_UPGRADE_KIT = register(
            "copper_upgrade_kit",
            CopperUpgradeItem::new,
            new Item.Settings().rarity(Rarity.UNCOMMON).maxDamage(64).repairable(Items.COPPER_INGOT)
    );

    private static Item register(String name) {
        return register(name, Item::new, new Item.Settings());
    }

    private static Item register(String name, Item.Settings settings) {
        return register(name, Item::new, settings);
    }

    private static Item register(String name, Function<Item.Settings, Item> factory) {
        return register(name, factory, new Item.Settings());
    }

    private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> itemKey = keyOf(name);
        Item item = factory.apply(settings.registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }

    private static RegistryKey<Item> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.ITEM, Copperworks.id(id));
    }

    public static void bootstrap() {
        Copperworks.LOGGER.info("Registering Items for {}", Copperworks.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.addAfter(Items.IRON_INGOT, IRON_PLATE);
            entries.addAfter(Items.COPPER_INGOT, COPPER_PLATE);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.addBefore(Items.NAME_TAG, COPPER_UPGRADE_KIT);
        });
    }
}
