package dev.mariany.copperworks.client.render.item.property.bool;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.Copperworks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.bool.BooleanProperties;
import net.minecraft.client.render.item.property.bool.BooleanProperty;

@Environment(EnvType.CLIENT)
public final class CWBooleanProperties {
    private CWBooleanProperties() {
    }

    public static void bootstrap() {
        register("bound", BoundProperty.CODEC);
    }

    private static void register(String name, MapCodec<? extends BooleanProperty> codec) {
        BooleanProperties.ID_MAPPER.put(Copperworks.id(name), codec);
    }
}
