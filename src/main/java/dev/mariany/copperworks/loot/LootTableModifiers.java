package dev.mariany.copperworks.loot;

import dev.mariany.copperworks.item.CWItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetDamageLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;

import java.util.List;

public class LootTableModifiers {
    private static final List<RegistryKey<LootTable>> CONTAINS_COPPER_UPGRADE = List.of(
            LootTables.ABANDONED_MINESHAFT_CHEST,
            LootTables.PILLAGER_OUTPOST_CHEST,
            LootTables.VILLAGE_ARMORER_CHEST,
            LootTables.VILLAGE_TOOLSMITH_CHEST,
            LootTables.VILLAGE_WEAPONSMITH_CHEST
    );

    private LootTableModifiers() {}

    public static void modifyLootTables() {
        LootTableEvents.MODIFY.register(
                (key, tableBuilder, source, registries) -> {
                    if (source.isBuiltin()) {
                        if (CONTAINS_COPPER_UPGRADE.contains(key)) {
                            tableBuilder.pool(
                                    LootPool.builder()
                                            .rolls(UniformLootNumberProvider.create(0, 2))
                                            .conditionally(RandomChanceLootCondition.builder(0.7F))
                                            .with(
                                                    ItemEntry.builder(CWItems.COPPER_UPGRADE_KIT)
                                                             .apply(
                                                                     SetDamageLootFunction.builder(
                                                                             UniformLootNumberProvider.create(0.1F, 1)
                                                                     )
                                                             )
                                            )
                                            .build()
                            );
                        }
                    }
                }
        );
    }
}
