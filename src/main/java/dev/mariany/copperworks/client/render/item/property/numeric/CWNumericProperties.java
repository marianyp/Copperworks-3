package dev.mariany.copperworks.client.render.item.property.numeric;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.Copperworks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.numeric.NumericProperties;
import net.minecraft.client.render.item.property.numeric.NumericProperty;

@Environment(EnvType.CLIENT)
public interface CWNumericProperties {
    static void bootstrap() {
        register("dragon_breath_fullness", DragonBreathFullnessProperty.CODEC);
    }

    private static void register(String name, MapCodec<? extends NumericProperty> codec) {
        NumericProperties.ID_MAPPER.put(Copperworks.id(name), codec);
    }
}
