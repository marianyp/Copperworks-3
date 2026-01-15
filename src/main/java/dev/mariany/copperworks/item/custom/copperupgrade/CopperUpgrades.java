package dev.mariany.copperworks.item.custom.copperupgrade;

import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.registry.CWRegistryKeys;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class CopperUpgrades {
    private CopperUpgrades() {
    }
    
    public static Optional<CopperUpgrade> getCopperUpgrade(DynamicRegistryManager wrapperLookup, BlockState state) {
        Optional<RegistryKey<Block>> optionalBlockKey = state.getRegistryEntry().getKey();

        Optional<Registry<CopperUpgrade>> optionalCopperUpgradesRegistry = wrapperLookup.getOptional(
                CWRegistryKeys.COPPER_UPGRADE
        );

        if (optionalBlockKey.isPresent() && optionalCopperUpgradesRegistry.isPresent()) {
            RegistryKey<Block> blockKey = optionalBlockKey.get();
            Registry<CopperUpgrade> copperUpgradesRegistry = optionalCopperUpgradesRegistry.get();
            return Optional.ofNullable(copperUpgradesRegistry.get(blockKey.getValue()));
        }

        return Optional.empty();
    }

    public static void boostrap(Registerable<CopperUpgrade> registry) {
        register(registry, CWBlocks.WOODEN_RAIL, CWBlocks.COPPER_RAIL, Properties.RAIL_SHAPE, Properties.WATERLOGGED);
        register(registry, Blocks.LEVER, CWBlocks.COPPER_LEVER, Properties.FACING, Properties.BLOCK_FACE);
        register(registry, Blocks.SCAFFOLDING, CWBlocks.COPPER_SCAFFOLDING);
        register(registry, Blocks.BARREL, CWBlocks.COPPER_BARREL, true, Properties.FACING);
        register(
                registry,
                Blocks.CHEST,
                Blocks.COPPER_CHEST,
                true,
                Properties.FACING,
                Properties.CHEST_TYPE,
                Properties.WATERLOGGED
        );
    }

    private static void register(
            Registerable<CopperUpgrade> registry,
            Block from,
            Block to,
            Property<?>... properties
    ) {
        register(registry, from, to, false, properties);
    }

    private static void register(
            Registerable<CopperUpgrade> registry,
            Block from,
            Block to,
            boolean mergeInventories,
            Property<?>... properties
    ) {
        from.getDefaultState().getRegistryEntry()
            .getKey()
            .ifPresent(registryKey -> {
                RegistryKey<CopperUpgrade> key = RegistryKey.of(
                        CWRegistryKeys.COPPER_UPGRADE,
                        registryKey.getValue()
                );

                Set<String> propertyNames = Arrays.stream(properties)
                                                  .map(Property::getName)
                                                  .collect(Collectors.toSet());

                registry.register(key, new CopperUpgrade(to, propertyNames, mergeInventories));
            });
    }
}
