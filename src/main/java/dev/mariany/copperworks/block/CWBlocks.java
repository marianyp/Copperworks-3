package dev.mariany.copperworks.block;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.custom.AlternativeScaffoldingBlock;
import dev.mariany.copperworks.block.custom.BatteryBlock;
import dev.mariany.copperworks.block.custom.TimedLeverBlock;
import dev.mariany.copperworks.block.custom.barrel.CopperBarrelBlock;
import dev.mariany.copperworks.block.custom.clock.CopperClockBlock;
import dev.mariany.copperworks.block.custom.muffler.MufflerBlock;
import dev.mariany.copperworks.block.custom.rail.CopperRailBlock;
import dev.mariany.copperworks.block.custom.rail.WoodenRailBlock;
import dev.mariany.copperworks.block.custom.relay.RelayBlock;
import dev.mariany.copperworks.block.custom.relay.bound.BoundRelayBlock;
import dev.mariany.copperworks.block.custom.relay.ender.EnderRelayBlock;
import dev.mariany.copperworks.block.custom.relay.radio.RadioRelayBlock;
import dev.mariany.copperworks.block.custom.sensor.SensorBlock;
import dev.mariany.copperworks.event.block.BlockEvents;
import dev.mariany.copperworks.item.custom.AlternativeScaffoldingBlockItem;
import dev.mariany.copperworks.item.custom.WrenchItem;
import dev.mariany.copperworks.item.custom.copperupgrade.CopperUpgradeItem;
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
            WoodenRailBlock::new,
            AbstractBlock.Settings
                    .create()
                    .noCollision()
                    .strength(0.4F)
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .sounds(BlockSoundGroup.LADDER)
    );

    public static final Block COPPER_RAIL = register(
            "copper_rail",
            CopperRailBlock::new,
            AbstractBlock.Settings.copy(Blocks.RAIL).sounds(BlockSoundGroup.COPPER)
    );

    public static final Block COPPER_LEVER = register(
            "copper_lever",
            settings -> new TimedLeverBlock(CWSoundEvents.BLOCK_COPPER_LEVER_CLICK, settings, 60),
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

    public static final Block COPPER_BARREL = register(
            "copper_barrel",
            CopperBarrelBlock::new,
            genericCopperSettings()
    );

    public static final Block COPPER_CLOCK = register(
            "copper_clock",
            CopperClockBlock::new,
            genericCopperSettings().solidBlock(Blocks::never)
    );

    public static final Block BATTERY = register(
            "battery",
            BatteryBlock::new,
            genericCopperSettings().solidBlock(Blocks::never)
    );

    public static final Block RELAY = register(
            "relay",
            RelayBlock::new,
            genericCopperSettings().solidBlock(Blocks::never)
    );

    public static final Block BOUND_RELAY = register(
            "bound_relay",
            BoundRelayBlock::new,
            AbstractBlock.Settings.copy(RELAY)
    );

    public static final Block RADIO_RELAY = register(
            "radio_relay",
            RadioRelayBlock::new,
            AbstractBlock.Settings.copy(RELAY)
    );

    public static final Block ENDER_RELAY = register(
            "ender_relay",
            EnderRelayBlock::new,
            AbstractBlock.Settings.copy(RELAY)
    );

    public static final Block COPPER_SENSOR = register(
            "copper_sensor",
            SensorBlock::new,
            genericCopperSettings().solidBlock(Blocks::never)
    );

    public static final Block MUFFLER = register(
            "muffler",
            MufflerBlock::new,
            genericCopperSettings().solidBlock(Blocks::never)
    );

    private CWBlocks() {}

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
        Copperworks.bootstrapLog("Blocks");

        BlockEvents.OVERRIDE_BLOCK_INTERACTION.register(CopperUpgradeItem::shouldOverrideInteraction);
        BlockEvents.OVERRIDE_BLOCK_INTERACTION.register(WrenchItem::shouldOverrideInteraction);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.addBefore(Items.RAIL, WOODEN_RAIL);
            entries.addAfter(WOODEN_RAIL, COPPER_RAIL);

            entries.addAfter(Items.LEVER, COPPER_LEVER);

            entries.addAfter(Items.BARREL, COPPER_BARREL);

            entries.addAfter(Items.TARGET, COPPER_CLOCK);

            entries.addAfter(Items.REDSTONE_BLOCK, RELAY);

            entries.addAfter(RELAY, BATTERY);

            entries.addBefore(Items.OBSERVER, COPPER_SENSOR);

            entries.addAfter(Items.NOTE_BLOCK, MUFFLER);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.addAfter(Items.WAXED_OXIDIZED_COPPER_BULB, STICKY_COPPER);
            entries.addAfter(STICKY_COPPER, STICKY_COPPER_HONEY);

            entries.addAfter(Items.SCAFFOLDING, COPPER_SCAFFOLDING);

            entries.addAfter(Items.BARREL, COPPER_BARREL);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.addBefore(Items.RAIL, WOODEN_RAIL);
            entries.addAfter(WOODEN_RAIL, COPPER_RAIL);
        });
    }
}
