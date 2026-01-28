package dev.mariany.copperworks.gamerule;

import dev.mariany.copperworks.Copperworks;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;

public class CWGamerules {
    public static final GameRule<Boolean> WATER_POTIONS_DEGRADE_BLOCKS = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.MISC)
            .buildAndRegister(Copperworks.id("water_potions_degrade_blocks"));

    private CWGamerules() {
    }

    public static void bootstrap() {
        Copperworks.bootstrapLog("Gamerules");
    }
}
