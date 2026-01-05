package dev.mariany.copperworks.advancement.criterion;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class CWCriterion {
    public static final TickCriterion USE_COPPER_UPGRADE_KIT = register(
            "use_copper_upgrade_kit",
            new TickCriterion()
    );

    public static final TickCriterion BOUND_RELAY = register(
            "bound_relay",
            new TickCriterion()
    );

    public static <T extends Criterion<?>> T register(String name, T criterion) {
        return Registry.register(Registries.CRITERION, Copperworks.id(name), criterion);
    }

    public static void bootstrap() {
        Copperworks.bootstrapLog("Criteria");
    }
}
