package dev.mariany.copperworks.datagen;

import dev.mariany.copperworks.item.CWItems;
import dev.mariany.copperworks.tag.CWTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class CWItemTagProviders extends FabricTagProvider.ItemTagProvider {
    public CWItemTagProviders(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        valueLookupBuilder(ItemTags.FOOT_ARMOR).add(CWItems.ROCKET_BOOTS);
        valueLookupBuilder(CWTags.Items.REPAIRS_ROCKET_BOOTS).add(Items.BLAZE_POWDER);
    }
}
