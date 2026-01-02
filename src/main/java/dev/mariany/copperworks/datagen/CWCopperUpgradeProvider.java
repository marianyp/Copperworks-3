package dev.mariany.copperworks.datagen;

import dev.mariany.copperworks.item.custom.copperupgrade.CopperUpgrade;
import dev.mariany.copperworks.registry.CWRegistryKeys;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class CWCopperUpgradeProvider extends FabricDynamicRegistryProvider {
    public CWCopperUpgradeProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        RegistryWrapper.Impl<CopperUpgrade> registry = registries.getOrThrow(CWRegistryKeys.COPPER_UPGRADE);
        registry.streamKeys().forEach(key -> entries.add(registry, key));
    }

    @Override
    public String getName() {
        return "Copper Upgrades";
    }
}
