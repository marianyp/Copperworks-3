package dev.mariany.copperworks.client.render.item.property.bool;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.client.render.item.property.bool.BooleanProperties;

public interface CWBooleanProperties {
    static void bootstrap() {
        BooleanProperties.ID_MAPPER.put(Copperworks.id("bound"), BoundProperty.CODEC);
    }
}
