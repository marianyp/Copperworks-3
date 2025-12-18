package dev.mariany.copperworks.datagen;

import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.item.CWItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;

public class CWModelProvider extends FabricModelProvider {
    public CWModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerTurnableRail(CWBlocks.WOODEN_RAIL);
        blockStateModelGenerator.registerTurnableRail(CWBlocks.COPPER_RAIL);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(CWItems.COPPER_UPGRADE_KIT, Models.GENERATED);
    }
}
