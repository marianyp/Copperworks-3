package dev.mariany.copperworks.datagen;

import dev.mariany.copperworks.block.CWBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CWBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    private static final List<Block> AXE_MINEABLE = List.of(CWBlocks.WOODEN_RAIL);
    private static final List<Block> RAILS = List.of(CWBlocks.WOODEN_RAIL);

    public CWBlockTagProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        for (Block block : AXE_MINEABLE) {
            valueLookupBuilder(BlockTags.AXE_MINEABLE).add(block);
        }

        for (Block block : RAILS) {
            valueLookupBuilder(BlockTags.RAILS).add(block);
        }
    }
}
