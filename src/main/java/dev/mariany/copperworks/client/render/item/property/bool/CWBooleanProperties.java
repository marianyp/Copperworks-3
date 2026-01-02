package dev.mariany.copperworks.client.render.item.property.bool;

import dev.mariany.copperworks.Copperworks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.property.bool.BooleanProperties;

@Environment(EnvType.CLIENT)
public interface CWBooleanProperties {
    static void bootstrap() {
        BooleanProperties.ID_MAPPER.put(Copperworks.id("bound"), BoundProperty.CODEC);
    }
}
