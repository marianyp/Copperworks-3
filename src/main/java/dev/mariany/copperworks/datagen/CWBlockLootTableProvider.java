package dev.mariany.copperworks.datagen;

import dev.mariany.copperworks.block.CWBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class CWBlockLootTableProvider extends FabricBlockLootTableProvider {
    public CWBlockLootTableProvider(
            FabricDataOutput dataOutput,
            CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup
    ) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(CWBlocks.WOODEN_RAIL);
        addDrop(CWBlocks.COPPER_RAIL);
    }
}
