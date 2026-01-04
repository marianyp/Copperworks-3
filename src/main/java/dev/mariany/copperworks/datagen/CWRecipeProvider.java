package dev.mariany.copperworks.datagen;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.item.CWItems;
import dev.mariany.copperworks.recipe.MergeDragonBreathFullnessRecipe;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.recipe.ComplexRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
                this.createMergeDragonBreathRecipe();
                this.createEnderPowderRecipe();
            }

            private void createEnderPowderRecipe() {
                this.createShapeless(RecipeCategory.MISC, CWItems.ENDER_POWDER)
                    .input(Items.BLAZE_POWDER)
                    .input(Items.DRAGON_BREATH)
                    .criterion(hasItem(Items.DRAGON_BREATH), conditionsFromItem(Items.DRAGON_BREATH))
                    .offerTo(this.exporter);
            }

            private void createMergeDragonBreathRecipe() {
                ComplexRecipeJsonBuilder.create(MergeDragonBreathFullnessRecipe::new)
                                        .offerTo(this.exporter, "merge_dragon_breath");
            }

            private void createRocketBootsRecipe() {
                this.createShaped(RecipeCategory.COMBAT, CWItems.ROCKET_BOOTS)
                    .pattern("P P")
                    .pattern("N N")
                    .pattern("E E")
                    .input('P', CWItems.COPPER_PLATE)
                    .input('N', Items.NETHERITE_INGOT)
                    .input('E', CWItems.ENDER_POWDER)
                    .criterion(hasItem(CWItems.ENDER_POWDER), this.conditionsFromItem(CWItems.ENDER_POWDER))
                    .offerTo(this.exporter);
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
