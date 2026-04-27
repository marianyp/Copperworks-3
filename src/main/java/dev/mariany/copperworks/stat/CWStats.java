package dev.mariany.copperworks.stat;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public class CWStats {
    public static final Identifier OPEN_COPPER_BARREL = register("open_copper_barrel");
    public static final Identifier INTERACT_WITH_COPPER_CLOCK = register("interact_with_copper_clock");
    public static final Identifier INTERACT_WITH_SENSOR = register("interact_with_sensor");
    public static final Identifier RELAY_TELEPORTS = register("relay_teleports");

    private CWStats() {
    }

    private static Identifier register(String id) {
        return register(id, StatFormatter.DEFAULT);
    }

    private static Identifier register(String id, StatFormatter formatter) {
        return register(Copperworks.id(id), formatter);
    }

    private static Identifier register(Identifier id, StatFormatter formatter) {
        Registry.register(Registries.CUSTOM_STAT, id, id);
        Stats.CUSTOM.getOrCreateStat(id, formatter);
        return id;
    }

    public static void bootstrap() {
        Copperworks.bootstrapLog("Stats");
    }
}
