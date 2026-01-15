package dev.mariany.copperworks.recipe;

import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.item.custom.radio.RadioItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class RadioCloningRecipe extends SpecialCraftingRecipe {
    public RadioCloningRecipe(CraftingRecipeCategory craftingRecipeCategory) {
        super(craftingRecipeCategory);
    }

    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.getStackCount() != 2) {
            return false;
        }

        boolean target = false;
        boolean source = false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);

            if (stack.isEmpty() || !(stack.getItem() instanceof RadioItem)) {
                return false;
            }

            if (stack.contains(CWComponents.RELAY_POSITION)) {
                if (source) {
                    return false;
                }

                source = true;
            } else {
                target = true;
            }
        }

        return source && target;
    }

    protected static ItemStack findSourceRadio(CraftingRecipeInput input) {
        return input.getStacks().stream()
                    .filter(stack -> stack.contains(CWComponents.RELAY_POSITION))
                    .findFirst()
                    .orElse(ItemStack.EMPTY);
    }

    protected static ItemStack findTargetRadio(CraftingRecipeInput input) {
        return input.getStacks().stream()
                    .filter(stack -> !stack.contains(CWComponents.RELAY_POSITION))
                    .findFirst()
                    .orElse(ItemStack.EMPTY);
    }

    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack sourceStack = findSourceRadio(input);
        ItemStack targetStack = findTargetRadio(input);

        GlobalPos relayPosition = sourceStack.get(CWComponents.RELAY_POSITION);

        if (!sourceStack.isEmpty() && !targetStack.isEmpty() && relayPosition != null) {
            ItemStack resultStack = targetStack.copy();
            resultStack.set(CWComponents.RELAY_POSITION, relayPosition);
            return resultStack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input) {
        DefaultedList<ItemStack> remainders = DefaultedList.ofSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);

            if (stack.get(CWComponents.RELAY_POSITION) != null) {
                remainders.set(i, stack.copy());
            }
        }

        return remainders;
    }

    @Override
    public RecipeSerializer<RadioCloningRecipe> getSerializer() {
        return CWRecipeSerializers.RADIO_CLONING;
    }
}
