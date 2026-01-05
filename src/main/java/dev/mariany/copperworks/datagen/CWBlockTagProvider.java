package dev.mariany.copperworks.datagen;

import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.tag.CWTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class CWBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public CWBlockTagProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        valueLookupBuilder(BlockTags.PICKAXE_MINEABLE).add(
                CWBlocks.COPPER_LEVER,
                CWBlocks.STICKY_COPPER,
                CWBlocks.STICKY_COPPER_HONEY,
                CWBlocks.COPPER_SCAFFOLDING,
                CWBlocks.COPPER_BARREL,
                CWBlocks.COPPER_CLOCK,
                CWBlocks.RELAY,
                CWBlocks.BOUND_RELAY,
                CWBlocks.RADIO_RELAY
        );

        valueLookupBuilder(BlockTags.AXE_MINEABLE).add(CWBlocks.WOODEN_RAIL);

        valueLookupBuilder(BlockTags.NEEDS_STONE_TOOL).add(
                CWBlocks.COPPER_LEVER,
                CWBlocks.STICKY_COPPER,
                CWBlocks.STICKY_COPPER_HONEY,
                CWBlocks.COPPER_SCAFFOLDING,
                CWBlocks.COPPER_BARREL,
                CWBlocks.COPPER_CLOCK,
                CWBlocks.RELAY,
                CWBlocks.BOUND_RELAY,
                CWBlocks.RADIO_RELAY
        );

        valueLookupBuilder(BlockTags.RAILS).add(
                CWBlocks.WOODEN_RAIL,
                CWBlocks.COPPER_RAIL
        );

        valueLookupBuilder(CWTags.Blocks.STICKY).add(
                CWBlocks.STICKY_COPPER,
                CWBlocks.STICKY_COPPER_HONEY
        );

        valueLookupBuilder(CWTags.Blocks.EXTENDS_BATTERY_PULSE).addOptionalTag(
                CWTags.Blocks.EXTENDS_BATTERY_PULSE_SAME_AXIS
        );

        valueLookupBuilder(CWTags.Blocks.EXTENDS_BATTERY_PULSE_SAME_AXIS).addOptionalTag(
                BlockTags.LIGHTNING_RODS
        );
    }
}
