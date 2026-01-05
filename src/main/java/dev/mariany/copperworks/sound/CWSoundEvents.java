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
    public static final SoundEvent BLOCK_COPPER_BARREL_LID_MOVED = register("block.copper_barrel.lid_moved");
    public static final SoundEvent BLOCK_CLOCK_INTERACT = register("block.clock.interact");
    public static final SoundEvent BLOCK_RELAY_INSERT = register("block.relay.insert");
    public static final SoundEvent BLOCK_BOUND_RELAY_DISCONNECT = register("block.bound_relay.disconnect");
    public static final RegistryEntry.Reference<SoundEvent> ITEM_RADIO = registerReference("item.radio");
    public static final RegistryEntry.Reference<SoundEvent> ITEM_ARMOR_EQUIP_ROCKET_BOOTS = registerReference(
            "item.armor.equip.rocket_boots"
    );

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
        Copperworks.bootstrapLog("Sound Events");
    }
}
