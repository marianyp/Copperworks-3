package dev.mariany.copperworks.datagen;

import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.item.CWItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class CWRecipeProvider extends FabricRecipeProvider {
    public CWRecipeProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(
            RegistryWrapper.WrapperLookup wrapperLookup,
            RecipeExporter recipeExporter
    ) {
        return new RecipeGenerator(wrapperLookup, recipeExporter) {
            @Override
            public void generate() {
                this.createPlateRecipe(CWItems.COPPER_PLATE, Items.COPPER_INGOT);
                this.createPlateRecipe(CWItems.IRON_PLATE, Items.IRON_INGOT);
                this.createWoodenRailRecipe();
                this.createCopperLeverRecipe();
            }

            private void createCopperLeverRecipe() {
                this.createShaped(RecipeCategory.REDSTONE, CWBlocks.COPPER_LEVER)
                    .pattern("C")
                    .pattern("R")
                    .input('R', Items.LIGHTNING_ROD)
                    .input('C', Items.COBBLED_DEEPSLATE)
                    .criterion(hasItem(Items.LIGHTNING_ROD), conditionsFromItem(Items.LIGHTNING_ROD))
                    .offerTo(this.exporter);
            }

            private void createWoodenRailRecipe() {
                this.createShaped(RecipeCategory.TRANSPORTATION, CWBlocks.WOODEN_RAIL)
                    .pattern("P P")
                    .pattern("PSP")
                    .pattern("P P")
                    .input('P', ItemTags.PLANKS)
                    .input('S', Items.STICK)
                    .criterion(hasItem(Items.MINECART), conditionsFromItem(Items.MINECART))
                    .offerTo(this.exporter);
            }

            private void createPlateRecipe(Item plate, Item ingot) {
                this.createShaped(RecipeCategory.MISC, plate)
                    .pattern("II")
                    .pattern("II")
                    .input('I', ingot)
                    .criterion(hasItem(ingot), conditionsFromItem(ingot))
                    .offerTo(this.exporter);
            }
        };
    }

    @Override
    public String getName() {
        return "Copperworks Recipes";
    }
}
