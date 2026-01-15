package dev.mariany.copperworks.recipe;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class CWRecipeSerializers {
    public static final RecipeSerializer<RadioCloningRecipe> RADIO_CLONING = register(
            "crafting_special_radiocloning",
            new SpecialCraftingRecipe.SpecialRecipeSerializer<>(RadioCloningRecipe::new)
    );

    private CWRecipeSerializers() {
    }

    private static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String id, S serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Copperworks.id(id), serializer);
    }

    public static void bootstrap() {
        Copperworks.bootstrapLog("Recipe Serializers");
    }
}
