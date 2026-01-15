package dev.mariany.copperworks.datagen;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.item.CWItems;
import dev.mariany.copperworks.recipe.RadioCloningRecipe;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.recipe.ComplexRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
                ComplexRecipeJsonBuilder.create(RadioCloningRecipe::new).offerTo(this.exporter, "radio_cloning");

                this.createPlateRecipe(CWItems.COPPER_PLATE, Items.COPPER_INGOT);
                this.createPlateRecipe(CWItems.IRON_PLATE, Items.IRON_INGOT);
                this.createWoodenRailRecipe();
                this.createStickyCopperRecipe(Items.SLIME_BALL, CWBlocks.STICKY_COPPER);
                this.createStickyCopperRecipe(Items.HONEY_BOTTLE, CWBlocks.STICKY_COPPER_HONEY);
                this.createCopperClockRecipe();
                this.createCopperBatteryRecipe();
                this.createRelayRecipe();
                this.createRadioRecipe();
                this.createCopperLeverRecipe();
                this.createCopperScaffoldingRecipe();
                this.createRocketBootsRecipe();
                this.createCopperSensorRecipe();
                this.createMufflerRecipe();
                this.createWrenchRecipe();
            }

            private void createWrenchRecipe() {
                this.createShaped(RecipeCategory.TOOLS, CWItems.WRENCH)
                    .pattern("C C")
                    .pattern(" C ")
                    .pattern(" C ")
                    .input('C', Items.COPPER_INGOT)
                    .criterion(hasItem(Items.COPPER_INGOT), this.conditionsFromItem(Items.COPPER_INGOT))
                    .offerTo(this.exporter);
            }

            private void createMufflerRecipe() {
                this.createShaped(RecipeCategory.REDSTONE, CWBlocks.MUFFLER)
                    .pattern("CWC")
                    .pattern("WNW")
                    .pattern("CWC")
                    .input('C', CWItems.COPPER_PLATE)
                    .input('W', ItemTags.WOOL)
                    .input('N', Items.NOTE_BLOCK)
                    .criterion(hasItem(Items.NOTE_BLOCK), this.conditionsFromItem(Items.NOTE_BLOCK))
                    .offerTo(this.exporter);
            }

            private void createCopperSensorRecipe() {
                this.createShaped(RecipeCategory.REDSTONE, CWBlocks.COPPER_SENSOR)
                    .pattern("CEC")
                    .input('C', CWItems.COPPER_PLATE)
                    .input('E', Items.ENDER_EYE)
                    .criterion(hasItem(Items.ENDER_EYE), this.conditionsFromItem(Items.ENDER_EYE))
                    .offerTo(this.exporter);
            }

            private void createRocketBootsRecipe() {
                Item rocketBoots = CWItems.ROCKET_BOOTS;

                SmithingTransformRecipeJsonBuilder
                        .create(
                                Ingredient.ofItem(CWItems.COPPER_UPGRADE_KIT),
                                Ingredient.ofItem(Items.NETHERITE_BOOTS),
                                Ingredient.ofItem(Items.BLAZE_POWDER),
                                RecipeCategory.COMBAT,
                                rocketBoots
                        )
                        .criterion(
                                hasItem(Items.NETHERITE_BOOTS),
                                this.conditionsFromItem(Items.NETHERITE_BOOTS)
                        )
                        .offerTo(this.exporter, getItemPath(rocketBoots) + "_smithing");
            }

            private void createCopperScaffoldingRecipe() {
                this.createShaped(RecipeCategory.BUILDING_BLOCKS, CWBlocks.COPPER_SCAFFOLDING, 4)
                    .pattern(" C ")
                    .pattern("C C")
                    .pattern(" C ")
                    .input('C', CWItems.COPPER_PLATE)
                    .criterion(hasItem(CWItems.COPPER_PLATE), this.conditionsFromItem(CWItems.COPPER_PLATE))
                    .offerTo(this.exporter);
            }

            private void createCopperLeverRecipe() {
                this.createShaped(RecipeCategory.REDSTONE, CWBlocks.COPPER_LEVER)
                    .pattern("C")
                    .pattern("D")
                    .input('C', Items.COPPER_INGOT)
                    .input('D', Blocks.COBBLED_DEEPSLATE)
                    .criterion(hasItem(Items.COPPER_INGOT), this.conditionsFromItem(Items.COPPER_INGOT))
                    .offerTo(this.exporter);
            }

            private void createRadioRecipe() {
                String group = "radio";

                this.createShaped(RecipeCategory.REDSTONE, CWItems.RADIO)
                    .pattern("R")
                    .pattern("C")
                    .input('R', Items.REDSTONE_TORCH)
                    .input('C', Items.COPPER_INGOT)
                    .criterion(hasItem(Items.REDSTONE_TORCH), this.conditionsFromItem(Items.REDSTONE_TORCH))
                    .group(group)
                    .offerTo(this.exporter);

                this.createShapeless(RecipeCategory.REDSTONE, CWItems.RADIO)
                    .input(CWItems.RADIO)
                    .criterion(hasItem(Items.REDSTONE_TORCH), this.conditionsFromItem(Items.REDSTONE_TORCH))
                    .group(group)
                    .offerTo(this.exporter, RegistryKey.of(RegistryKeys.RECIPE, Copperworks.id("reset_radio")));
            }

            private void createRelayRecipe() {
                this.createShaped(RecipeCategory.REDSTONE, CWBlocks.RELAY, 2)
                    .pattern("CDC")
                    .pattern("DRD")
                    .pattern("CDC")
                    .input('C', CWItems.COPPER_PLATE)
                    .input('D', Items.DIAMOND)
                    .input('R', Items.REDSTONE)
                    .criterion(hasItem(Items.DIAMOND), this.conditionsFromItem(Items.DIAMOND))
                    .offerTo(this.exporter);
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
                    .criterion(hasItem(Items.REDSTONE_TORCH), this.conditionsFromItem(Items.REDSTONE_TORCH))
                    .offerTo(this.exporter);
            }

            private void createCopperClockRecipe() {
                this.createShaped(RecipeCategory.REDSTONE, CWBlocks.COPPER_CLOCK)
                    .pattern("PPP")
                    .pattern("PCP")
                    .pattern("PPP")
                    .input('P', CWItems.COPPER_PLATE)
                    .input('C', Items.CLOCK)
                    .criterion(hasItem(Items.CLOCK), this.conditionsFromItem(Items.CLOCK))
                    .offerTo(this.exporter);
            }

            private void createStickyCopperRecipe(Item input, Block output) {
                this.createShaped(RecipeCategory.MISC, output)
                    .pattern("PPP")
                    .pattern("CIC")
                    .pattern("PPP")
                    .input('P', CWItems.COPPER_PLATE)
                    .input('C', Items.COPPER_INGOT)
                    .input('I', input)
                    .criterion(hasItem(CWItems.COPPER_PLATE), this.conditionsFromItem(CWItems.COPPER_PLATE))
                    .group("sticky_copper")
                    .offerTo(this.exporter);
            }

            private void createWoodenRailRecipe() {
                this.createShaped(RecipeCategory.TRANSPORTATION, CWBlocks.WOODEN_RAIL)
                    .pattern("P P")
                    .pattern("PSP")
                    .pattern("P P")
                    .input('P', ItemTags.PLANKS)
                    .input('S', Items.STICK)
                    .criterion(hasItem(Items.MINECART), this.conditionsFromItem(Items.MINECART))
                    .offerTo(this.exporter);
            }

            private void createPlateRecipe(Item plate, Item ingot) {
                this.createShaped(RecipeCategory.MISC, plate)
                    .pattern("II")
                    .pattern("II")
                    .input('I', ingot)
                    .criterion(hasItem(ingot), this.conditionsFromItem(ingot))
                    .offerTo(this.exporter);
            }
        };
    }

    @Override
    public String getName() {
        return "Copperworks Recipes";
    }
}
