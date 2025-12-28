package dev.mariany.copperworks.screen;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class CWScreenHandlers {
    public static final ScreenHandlerType<InventoryNetworkScreenHandler> INVENTORY_NETWORK = register(
            "inventory_network",
            InventoryNetworkScreenHandler::new
    );

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(
            String id,
            ScreenHandlerType.Factory<T> factory
    ) {
        return Registry.register(
                Registries.SCREEN_HANDLER,
                Copperworks.id(id),
                new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES)
        );
    }

    public static void bootstrap() {
        Copperworks.LOGGER.info("Registering Screen Handlers for {}", Copperworks.MOD_ID);
    }
}
