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

        addDrop(CWBlocks.COPPER_LEVER);

        addDrop(CWBlocks.STICKY_COPPER);
        addDrop(CWBlocks.STICKY_COPPER_HONEY);

        addDrop(CWBlocks.COPPER_SCAFFOLDING);

        addDrop(CWBlocks.COPPER_BARREL, this::nameableContainerDrops);

        addDrop(CWBlocks.COPPER_CLOCK);

        addDrop(CWBlocks.BATTERY);

        addDrop(CWBlocks.RELAY);
        addDrop(CWBlocks.BOUND_RELAY, CWBlocks.RELAY);
        addDrop(CWBlocks.RADIO_RELAY, CWBlocks.RELAY);
        addDrop(CWBlocks.ENDER_RELAY, CWBlocks.RELAY);

        addDrop(CWBlocks.COPPER_SENSOR);

        addDrop(CWBlocks.MUFFLER);
    }
}
