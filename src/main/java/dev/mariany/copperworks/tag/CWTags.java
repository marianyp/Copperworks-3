package dev.mariany.copperworks.tag;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class CWTags {
    public static final class Blocks {
        public static final TagKey<Block> STICKY = createTag("sticky");
        public static final TagKey<Block> EXTENDS_BATTERY_PULSE = createTag("extends_battery_pulse");
        public static final TagKey<Block> EXTENDS_BATTERY_PULSE_SAME_AXIS = createTag("extends_battery_pulse_same_axis");

        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Copperworks.id(name));
        }
    }

    public static final class Entities {
        public static final TagKey<EntityType<?>> STICKY_IMMUNE = createTag(
                "sticky_immune"
        );

        private static TagKey<EntityType<?>> createTag(String name) {
            return TagKey.of(RegistryKeys.ENTITY_TYPE, Copperworks.id(name));
        }
    }
}
