package dev.mariany.copperworks.advancement.criterion;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class CWCriterion {
    public static final TickCriterion UPGRADE_WOODEN_RAIL = register("upgrade_wooden_rail", new TickCriterion());

    public static <T extends Criterion<?>> T register(String name, T criterion) {
        return Registry.register(Registries.CRITERION, Copperworks.id(name), criterion);
    }

    public static void bootstrap() {
        Copperworks.LOGGER.info("Registering Criteria for {}", Copperworks.MOD_ID);
    }
}
