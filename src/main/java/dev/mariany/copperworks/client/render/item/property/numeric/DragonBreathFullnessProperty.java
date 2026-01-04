package dev.mariany.copperworks.client.render.item.property.numeric;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.item.custom.PartialDragonBreathItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record DragonBreathFullnessProperty() implements NumericProperty {
    public static final MapCodec<DragonBreathFullnessProperty> CODEC = MapCodec.unit(
            new DragonBreathFullnessProperty()
    );

    @Override
    public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable HeldItemContext context, int seed) {
        return PartialDragonBreathItem.getAmountFilled(stack);
    }

    @Override
    public MapCodec<DragonBreathFullnessProperty> getCodec() {
        return CODEC;
    }
}
