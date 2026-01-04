package dev.mariany.copperworks.recipe;

import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.item.custom.PartialDragonBreathItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MergeDragonBreathFullnessRecipe extends SpecialCraftingRecipe {
    public MergeDragonBreathFullnessRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Nullable
    private static Pair<ItemStack, ItemStack> findPair(CraftingRecipeInput input) {
        if (input.getStackCount() == 2) {
            ItemStack firstNonEmpty = null;

            for (int i = 0; i < input.size(); i++) {
                ItemStack stack = input.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    if (firstNonEmpty != null) {
                        return canCombineStacks(firstNonEmpty, stack) ? new Pair<>(firstNonEmpty, stack) : null;
                    }

                    firstNonEmpty = stack;
                }
            }

        }

        return null;
    }

    private static boolean canCombineStacks(ItemStack first, ItemStack second) {
        if (first.getCount() != 1 || second.getCount() != 1) {
            return false;
        }

        if (first.getItem() instanceof PartialDragonBreathItem && second.getItem() instanceof PartialDragonBreathItem) {
            return first.contains(CWComponents.DRAGON_BREATH_FULLNESS)
                    && second.contains(CWComponents.DRAGON_BREATH_FULLNESS);
        }

        return false;
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        return findPair(input) != null;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup wrapperLookup) {
        Pair<ItemStack, ItemStack> pair = findPair(input);

        if (pair == null) {
            return ItemStack.EMPTY;
        }

        ItemStack firstStack = pair.getLeft();
        ItemStack secondStack = pair.getRight();

        if (firstStack.getItem() instanceof PartialDragonBreathItem partialDragonBreathItem) {
            int fullnessA = firstStack.getOrDefault(CWComponents.DRAGON_BREATH_FULLNESS, 0);
            int fullnessB = secondStack.getOrDefault(CWComponents.DRAGON_BREATH_FULLNESS, 0);

            int combinedFullness = fullnessA + fullnessB;

            if (combinedFullness >= partialDragonBreathItem.getMaxFullness()) {
                return Items.DRAGON_BREATH.getDefaultStack();
            }

            ItemStack outStack = firstStack.getItem().getDefaultStack();
            outStack.set(CWComponents.DRAGON_BREATH_FULLNESS, combinedFullness);

            return outStack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<MergeDragonBreathFullnessRecipe> getSerializer() {
        return CWRecipeSerializers.MERGE_DRAGON_BREATH_FULLNESS;
    }
}
