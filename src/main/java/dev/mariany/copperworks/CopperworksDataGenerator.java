package dev.mariany.copperworks;

import dev.mariany.copperworks.datagen.CWBlockLootTableProvider;
import dev.mariany.copperworks.datagen.CWBlockTagProvider;
import dev.mariany.copperworks.datagen.CWModelProvider;
import dev.mariany.copperworks.datagen.CWRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class CopperworksDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(CWBlockLootTableProvider::new);
		pack.addProvider(CWBlockTagProvider::new);
		pack.addProvider(CWModelProvider::new);
		pack.addProvider(CWRecipeProvider::new);
	}
}
