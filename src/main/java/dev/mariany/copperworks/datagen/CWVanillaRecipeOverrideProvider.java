package dev.mariany.copperworks.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Item;
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
                this.resolvePlateConflicts(Items.IRON_INGOT, Items.IRON_BARS, Items.IRON_TRAPDOOR);
                this.resolvePlateConflicts(Items.COPPER_INGOT, Items.COPPER_BARS.unaffected(), Items.COPPER_TRAPDOOR);
            }

            private void resolvePlateConflicts(Item ingot, Item bars, Item trapdoor) {
                this.createShaped(RecipeCategory.DECORATIONS, bars, 16)
                    .input('I', ingot)
                    .pattern("I I")
                    .pattern("I I")
                    .pattern("I I")
                    .criterion(hasItem(ingot), conditionsFromItem(ingot))
                    .offerTo(exporter);

                this.createShaped(RecipeCategory.REDSTONE, trapdoor, 2)
                    .input('I', ingot)
                    .pattern("III")
                    .pattern("III")
                    .criterion(hasItem(ingot), conditionsFromItem(ingot))
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
