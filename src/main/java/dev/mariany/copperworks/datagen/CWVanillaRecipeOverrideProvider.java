package dev.mariany.copperworks.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class CWVanillaRecipeOverrideProvider extends FabricRecipeProvider {
    public CWVanillaRecipeOverrideProvider(
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
                this.alternativeIronTrapdoor();
                this.alternativeIronBars();
            }

            private void alternativeIronTrapdoor() {
                this.createShaped(RecipeCategory.REDSTONE, Blocks.IRON_TRAPDOOR, 2)
                    .input('I', Items.IRON_INGOT)
                    .pattern("III")
                    .pattern("III")
                    .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                    .offerTo(exporter);
            }

            private void alternativeIronBars() {
                this.createShaped(RecipeCategory.DECORATIONS, Blocks.IRON_BARS, 16)
                    .input('I', Items.IRON_INGOT)
                    .pattern("I I")
                    .pattern("I I")
                    .pattern("I I")
                    .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                    .offerTo(exporter);
            }
        };
    }

    @Override
    protected Identifier getRecipeIdentifier(Identifier identifier) {
        return Identifier.ofVanilla(identifier.getPath());
    }

    @Override
    public String getName() {
        return "Vanilla Recipe Override";
    }
}
