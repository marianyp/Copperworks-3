package dev.mariany.copperworks.block;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.item.AlternativeScaffoldingBlockItem;
import dev.mariany.copperworks.sound.CWSoundEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;

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

    public static final Block COPPER_LEVER = register(
            "copper_lever",
            settings -> new TimedLeverBlock(CWSoundEvents.BLOCK_COPPER_LEVER_CLICK, settings),
            AbstractBlock.Settings
                    .create()
                    .noCollision()
                    .strength(2)
                    .requiresTool()
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .sounds(BlockSoundGroup.COPPER)
    );

    public static final Block STICKY_COPPER = register("sticky_copper", genericCopperSettings());

    public static final Block STICKY_COPPER_HONEY = register(
            "sticky_copper_honey",
            AbstractBlock.Settings.copy(STICKY_COPPER)
    );

    public static final Block COPPER_SCAFFOLDING = registerScaffolding(
            "copper_scaffolding",
            genericCopperSettings()
                    .strength(2)
                    .noCollision()
                    .solidBlock(Blocks::never)
                    .allowsSpawning(Blocks::never)
    );

    private static AbstractBlock.Settings genericCopperSettings() {
        return AbstractBlock.Settings.create()
                                     .mapColor(MapColor.ORANGE)
                                     .requiresTool()
                                     .strength(3F, 6F)
                                     .sounds(BlockSoundGroup.COPPER);
    }

    private static RegistryKey<Block> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.BLOCK, Copperworks.id(id));
    }

    private static Block register(String name, AbstractBlock.Settings settings) {
        return register(name, Block::new, settings);
    }

    private static Block register(
            String name,
            Function<AbstractBlock.Settings, Block> factory,
            AbstractBlock.Settings settings
    ) {
        Block block = Blocks.register(keyOf(name), factory, settings);
        Items.register(block);
        return block;
    }

    private static Block registerScaffolding(String name, AbstractBlock.Settings settings) {
        Block alternativeScaffoldingBlock = Blocks.register(keyOf(name), AlternativeScaffoldingBlock::new, settings);
        Items.register(alternativeScaffoldingBlock, AlternativeScaffoldingBlockItem::new);
        return alternativeScaffoldingBlock;
    }

    public static void bootstrap() {
        Copperworks.LOGGER.info("Registering Blocks for {}", Copperworks.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.addBefore(Items.RAIL, WOODEN_RAIL);
            entries.addAfter(WOODEN_RAIL, COPPER_RAIL);

            entries.addAfter(Items.LEVER, COPPER_LEVER);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.addAfter(Items.WAXED_OXIDIZED_COPPER_BULB, STICKY_COPPER);
            entries.addAfter(STICKY_COPPER, STICKY_COPPER_HONEY);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.addBefore(Items.RAIL, WOODEN_RAIL);
            entries.addAfter(WOODEN_RAIL, COPPER_RAIL);
        });
    }
}
