package dev.mariany.copperworks;

import dev.mariany.copperworks.datagen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class CopperworksDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(CWEntityTagProvider::new);
		pack.addProvider(CWBlockLootTableProvider::new);
		pack.addProvider(CWBlockTagProvider::new);
		pack.addProvider(CWModelProvider::new);
		pack.addProvider(CWRecipeProvider::new);
	}
}
