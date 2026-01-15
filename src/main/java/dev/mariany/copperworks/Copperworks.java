package dev.mariany.copperworks;

import dev.mariany.copperworks.advancement.criterion.CWCriterion;
import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.block.PotionDegradation;
import dev.mariany.copperworks.event.entity.EntityEvents;
import dev.mariany.copperworks.block.DynamicRails;
import dev.mariany.copperworks.event.server.ServerTickEventsHandler;
import dev.mariany.copperworks.gamerule.CWGamerules;
import dev.mariany.copperworks.item.CWItems;
import dev.mariany.copperworks.item.custom.copperupgrade.CopperUpgrade;
import dev.mariany.copperworks.loot.LootTableModifiers;
import dev.mariany.copperworks.packet.CWPackets;
import dev.mariany.copperworks.packet.serverbound.ServerboundPackets;
import dev.mariany.copperworks.recipe.CWRecipeSerializers;
import dev.mariany.copperworks.registry.CWRegistryKeys;
import dev.mariany.copperworks.screen.CWScreenHandlers;
import dev.mariany.copperworks.server.world.CWChunkTickets;
import dev.mariany.copperworks.sound.CWSoundEvents;
import dev.mariany.copperworks.stat.CWStats;
import dev.mariany.copperworks.world.poi.CWPointOfInterestTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Copperworks implements ModInitializer {
    public static final String MOD_ID = "copperworks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String resource) {
        return Identifier.of(Copperworks.MOD_ID, resource);
    }

    public static void bootstrapLog(String type) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LOGGER.info("Registering {}", type);
        }
    }

    public static void infoLog(String msg, Object... arguments) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LOGGER.info(msg, arguments);
        }
    }

    public static void warnLog(String msg, Object... arguments) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LOGGER.warn(msg, arguments);
        }
    }

    @Override
    public void onInitialize() {
        DynamicRegistries.registerSynced(CWRegistryKeys.COPPER_UPGRADE, CopperUpgrade.CODEC);

        CWPackets.bootstrap();
        ServerboundPackets.bootstrap();

        CWRecipeSerializers.bootstrap();
        CWChunkTickets.bootstrap();
        CWGamerules.bootstrap();
        CWSoundEvents.bootstrap();
        CWCriterion.bootstrap();
        CWStats.bootstrap();
        CWScreenHandlers.bootstrap();
        CWComponents.bootstrap();
        CWItems.bootstrap();
        CWBlocks.bootstrap();
        CWBlockEntities.bootstrap();
        CWPointOfInterestTypes.bootstrap();

        LootTableModifiers.modifyLootTables();

        EntityEvents.BEFORE_MINECART_TRAVEL.register(DynamicRails::onMinecartTravel);
        ServerTickEvents.END_WORLD_TICK.register(ServerTickEventsHandler::onWorldTick);
        EntityEvents.BEFORE_POTION_COLLISION.register(PotionDegradation::onPotionCollision);
    }
}