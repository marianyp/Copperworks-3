package dev.mariany.copperworks;

import dev.mariany.copperworks.datagen.*;
import dev.mariany.copperworks.item.custom.copperupgrade.CopperUpgrades;
import dev.mariany.copperworks.registry.CWRegistryKeys;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraft.registry.RegistryBuilder;

public class CopperworksDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(CWBlockLootTableProvider::new);
        pack.addProvider(CWBlockTagProvider::new);
        pack.addProvider(CWCopperUpgradeProvider::new);
        pack.addProvider(CWEntityTagProvider::new);
        pack.addProvider(CWItemTagProviders::new);
        pack.addProvider(CWModelProvider::new);
        pack.addProvider(CWRecipeProvider::new);

        DataGenerator.Pack secondPack = fabricDataGenerator.createPack();
        secondPack.addProvider(CWEquipmentAssetProvider::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder registryBuilder) {
        registryBuilder.addRegistry(CWRegistryKeys.COPPER_UPGRADE, CopperUpgrades::boostrap);
    }
}
