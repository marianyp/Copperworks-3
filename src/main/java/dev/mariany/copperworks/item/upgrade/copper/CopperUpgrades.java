package dev.mariany.copperworks.item.upgrade.copper;

import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.registry.CWRegistryKeys;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public interface CopperUpgrades {
    static void boostrap(Registerable<CopperUpgrade> registry) {
        register(registry, CWBlocks.WOODEN_RAIL, CWBlocks.COPPER_RAIL, Properties.RAIL_SHAPE, Properties.WATERLOGGED);
        register(registry, Blocks.LEVER, CWBlocks.COPPER_LEVER, Properties.FACING, Properties.BLOCK_FACE);
        register(registry, Blocks.SCAFFOLDING, CWBlocks.COPPER_SCAFFOLDING);
    }

    private static void register(
            Registerable<CopperUpgrade> registry,
            Block from,
            Block to,
            Property<?>... properties
    ) {
        from.getRegistryEntry()
            .getKey()
            .ifPresent(registryKey -> {
                RegistryKey<CopperUpgrade> key = RegistryKey.of(
                        CWRegistryKeys.COPPER_UPGRADE,
                        registryKey.getValue()
                );

                Set<String> propertyNames = Arrays.stream(properties)
                                                  .map(Property::getName)
                                                  .collect(Collectors.toSet());

                registry.register(key, new CopperUpgrade(to, propertyNames));
            });
    }
}
