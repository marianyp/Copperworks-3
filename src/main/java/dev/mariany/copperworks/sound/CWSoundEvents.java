package dev.mariany.copperworks.sound;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class CWSoundEvents {
    public static final SoundEvent BLOCK_WOODEN_RAIL_BREAK = register("block.wooden_rail.break");
    public static final SoundEvent BLOCK_COPPER_LEVER_CLICK = register("block.copper_lever.click");
    public static final SoundEvent BLOCK_CLOCK_INTERACT = register("block.clock.interact");

    private static SoundEvent register(String id) {
        return register(Copperworks.id(id));
    }

    private static SoundEvent register(Identifier id) {
        return register(id, id);
    }

    private static SoundEvent register(Identifier id, Identifier soundId) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(soundId));
    }

    private static RegistryEntry.Reference<SoundEvent> registerReference(String id) {
        return registerReference(Copperworks.id(id));
    }

    private static RegistryEntry.Reference<SoundEvent> registerReference(Identifier id) {
        return registerReference(id, id);
    }

    private static RegistryEntry.Reference<SoundEvent> registerReference(Identifier id, Identifier soundId) {
        return Registry.registerReference(Registries.SOUND_EVENT, id, SoundEvent.of(soundId));
    }

    public static void bootstrap() {
        Copperworks.LOGGER.info("Registering Sound Events for {}", Copperworks.MOD_ID);
    }
}
