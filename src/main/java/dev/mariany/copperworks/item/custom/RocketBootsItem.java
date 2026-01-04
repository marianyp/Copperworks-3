package dev.mariany.copperworks.item.custom;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class RocketBootsItem extends Item {
    public RocketBootsItem(Settings settings) {
        super(settings);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        if (stack.willBreakNextUse()) {
            return 0;
        }

        return MathHelper.clamp(Math.round(13F - stack.getDamage() * 13F / stack.getMaxDamage()), 1, 13);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return super.getItemBarColor(stack);
    }
}
