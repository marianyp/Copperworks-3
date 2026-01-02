package dev.mariany.copperworks.client.render.item.property.bool;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.component.CWComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record BoundProperty() implements BooleanProperty {
    public static final MapCodec<BoundProperty> CODEC = MapCodec.unit(new BoundProperty());

    @Override
    public boolean test(
            ItemStack stack,
            @Nullable ClientWorld world,
            @Nullable LivingEntity entity,
            int seed,
            ItemDisplayContext displayContext
    ) {
        return stack.contains(CWComponents.RELAY_POSITION);
    }

    @Override
    public MapCodec<BoundProperty> getCodec() {
        return CODEC;
    }
}
