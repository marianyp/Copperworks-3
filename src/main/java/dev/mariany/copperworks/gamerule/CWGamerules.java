package dev.mariany.copperworks.gamerule;

import dev.mariany.copperworks.Copperworks;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class CWGamerules {
    private CWGamerules() {}

    public static final GameRules.Key<GameRules.BooleanRule> WATER_POTIONS_DEGRADE_BLOCKS = GameRuleRegistry.register(
            "waterPotionsDegradeBlocks",
            GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(true)
    );

    public static void bootstrap() {
        Copperworks.bootstrapLog("Gamerules");
    }
}
