package dev.mariany.copperworks.registry;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.item.upgrade.copper.CopperUpgrade;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class CWRegistryKeys {
    public static final RegistryKey<Registry<CopperUpgrade>> COPPER_UPGRADE = of("copper_upgrade");

    private static <T> RegistryKey<Registry<T>> of(String id) {
        return RegistryKey.ofRegistry(Copperworks.id(id));
    }
}
