package dev.mariany.copperworks;

import dev.mariany.copperworks.advancement.criterion.CWCriterion;
import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.event.block.UseBlockHandler;
import dev.mariany.copperworks.event.entity.EntityEvents;
import dev.mariany.copperworks.event.entity.MinecartEventHandler;
import dev.mariany.copperworks.event.server.ServerTickEventsHandler;
import dev.mariany.copperworks.item.CWItems;
import dev.mariany.copperworks.item.custom.copperupgrade.CopperUpgrade;
import dev.mariany.copperworks.loot.LootTableModifiers;
import dev.mariany.copperworks.packet.CWPackets;
import dev.mariany.copperworks.packet.serverbound.ServerboundPackets;
import dev.mariany.copperworks.recipe.CWRecipeSerializers;
import dev.mariany.copperworks.registry.CWRegistryKeys;
import dev.mariany.copperworks.screen.CWScreenHandlers;
import dev.mariany.copperworks.sound.CWSoundEvents;
import dev.mariany.copperworks.stat.CWStats;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Copperworks implements ModInitializer {
    public static final String MOD_ID = "copperworks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String resource) {
        return Identifier.of(Copperworks.MOD_ID, resource);
    }

    @Override
    public void onInitialize() {
        DynamicRegistries.registerSynced(CWRegistryKeys.COPPER_UPGRADE, CopperUpgrade.CODEC);

        CWPackets.bootstrap();
        ServerboundPackets.bootstrap();

        CWSoundEvents.bootstrap();
        CWCriterion.bootstrap();
        CWStats.bootstrap();
        CWScreenHandlers.bootstrap();
        CWComponents.bootstrap();
        CWItems.bootstrap();
        CWBlocks.bootstrap();
        CWBlockEntities.bootstrap();
        CWRecipeSerializers.bootstrap();

        LootTableModifiers.modifyLootTables();

        EntityEvents.BEFORE_MINECART_TRAVEL.register(MinecartEventHandler::onMinecartTravel);
        ServerTickEvents.END_WORLD_TICK.register(ServerTickEventsHandler::onWorldTick);
        UseBlockCallback.EVENT.register(UseBlockHandler::onUseBlock);
    }
}