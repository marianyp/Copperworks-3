package dev.mariany.copperworks.datagen;

import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.item.CWItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
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
                this.createStickyCopperRecipe(Items.SLIME_BALL, CWBlocks.STICKY_COPPER);
                this.createStickyCopperRecipe(Items.HONEY_BOTTLE, CWBlocks.STICKY_COPPER_HONEY);
                this.createCopperClockRecipe();
                this.createCopperBatteryRecipe();
            }

            private void createCopperBatteryRecipe() {
                this.createShaped(RecipeCategory.REDSTONE, CWBlocks.BATTERY)
                    .pattern("CCC")
                    .pattern("cRc")
                    .pattern("III")
                    .input('R', Items.REDSTONE_TORCH)
                    .input('c', Items.COPPER_INGOT)
                    .input('C', CWItems.COPPER_PLATE)
                    .input('I', CWItems.IRON_PLATE)
                    .criterion(hasItem(Items.REDSTONE_TORCH), conditionsFromItem(Items.REDSTONE_TORCH))
                    .offerTo(exporter);
            }

            private void createCopperClockRecipe() {
                this.createShaped(RecipeCategory.REDSTONE, CWBlocks.COPPER_CLOCK)
                    .pattern("PPP")
                    .pattern("PCP")
                    .pattern("PPP")
                    .input('P', CWItems.COPPER_PLATE)
                    .input('C', Items.CLOCK)
                    .criterion(hasItem(Items.CLOCK), conditionsFromItem(Items.CLOCK))
                    .offerTo(exporter);
            }

            private void createStickyCopperRecipe(Item input, Block output) {
                this.createShaped(RecipeCategory.MISC, output)
                    .pattern("PPP")
                    .pattern("CIC")
                    .pattern("PPP")
                    .input('P', CWItems.COPPER_PLATE)
                    .input('C', Items.COPPER_INGOT)
                    .input('I', input)
                    .criterion(hasItem(CWItems.COPPER_PLATE), conditionsFromItem(CWItems.COPPER_PLATE))
                    .group("sticky_copper")
                    .offerTo(exporter);
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
