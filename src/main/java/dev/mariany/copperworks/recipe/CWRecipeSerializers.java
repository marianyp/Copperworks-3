package dev.mariany.copperworks.recipe;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface CWRecipeSerializers {
    RecipeSerializer<MergeDragonBreathFullnessRecipe> MERGE_DRAGON_BREATH_FULLNESS = register(
            "crafting_special_merge_dragon_breath_fullness",
            new SpecialCraftingRecipe.SpecialRecipeSerializer<>(MergeDragonBreathFullnessRecipe::new)
    );

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String id, S serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Copperworks.id(id), serializer);
    }

    static void bootstrap() {
        Copperworks.LOGGER.info("Registering Recipe Serializers for {}", Copperworks.MOD_ID);
    }
}
