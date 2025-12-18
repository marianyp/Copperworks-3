package dev.mariany.copperworks.block;

import dev.mariany.copperworks.Copperworks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class CWBlocks {
    public static final Block WOODEN_RAIL = register(
            "wooden_rail",
            RailBlock::new,
            AbstractBlock.Settings
                    .create()
                    .noCollision()
                    .strength(0.4F)
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .sounds(BlockSoundGroup.LADDER)
    );

    public static final Block COPPER_RAIL = register(
            "copper_rail",
            RailBlock::new,
            AbstractBlock.Settings.copy(Blocks.RAIL).sounds(BlockSoundGroup.COPPER)
    );

    private static Block register(String name, AbstractBlock.Settings settings) {
        return register(name, Block::new, settings);
    }

    private static Block register(
            String name,
            Function<AbstractBlock.Settings, Block> factory,
            AbstractBlock.Settings settings
    ) {
        Identifier identifier = Copperworks.id(name);
        RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, identifier);

        Block block = Blocks.register(registryKey, factory, settings);
        Items.register(block);

        return block;
    }

    public static void bootstrap() {
        Copperworks.LOGGER.info("Registering Blocks for {}", Copperworks.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.addBefore(Items.RAIL, WOODEN_RAIL);
            entries.addAfter(WOODEN_RAIL, COPPER_RAIL);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.addBefore(Items.RAIL, WOODEN_RAIL);
            entries.addAfter(WOODEN_RAIL, COPPER_RAIL);
        });
    }
}
