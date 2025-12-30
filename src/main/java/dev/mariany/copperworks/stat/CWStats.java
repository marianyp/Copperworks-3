package dev.mariany.copperworks.stat;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public class CWStats {
    public static final Identifier OPEN_COPPER_BARREL = register("open_copper_barrel", StatFormatter.DEFAULT);
    public static final Identifier INTERACT_WITH_COPPER_CLOCK = register(
            "interact_with_copper_clock",
            StatFormatter.DEFAULT
    );

    private static Identifier register(String id, StatFormatter formatter) {
        Identifier identifier = Copperworks.id(id);
        Registry.register(Registries.CUSTOM_STAT, id, identifier);
        Stats.CUSTOM.getOrCreateStat(identifier, formatter);
        return identifier;
    }

    public static void bootstrap() {
        Copperworks.LOGGER.info("Registering Stats for {}", Copperworks.MOD_ID);
    }
}
