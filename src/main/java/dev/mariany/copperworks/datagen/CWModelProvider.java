package dev.mariany.copperworks.datagen;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.client.render.item.property.bool.BoundProperty;
import dev.mariany.copperworks.item.CWItems;
import dev.mariany.copperworks.item.equipment.CWEquipmentAssetKeys;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.client.data.*;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.json.WeightedVariant;
import net.minecraft.item.Item;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class CWModelProvider extends FabricModelProvider {
    public CWModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(CWItems.COPPER_PLATE, Models.GENERATED);
        itemModelGenerator.register(CWItems.IRON_PLATE, Models.GENERATED);

        itemModelGenerator.register(CWItems.COPPER_UPGRADE_KIT, Models.GENERATED);

        itemModelGenerator.register(CWItems.AMETHYST_PIECE, Models.GENERATED);

        itemModelGenerator.register(CWItems.PATINA, Models.GENERATED);

        itemModelGenerator.registerArmor(
                CWItems.ROCKET_BOOTS,
                CWEquipmentAssetKeys.ROCKET_BOOTS,
                ItemModelGenerator.BOOTS_TRIM_ID_PREFIX,
                false
        );

        registerRadio(itemModelGenerator);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerTurnableRail(CWBlocks.WOODEN_RAIL);
        blockStateModelGenerator.registerTurnableRail(CWBlocks.COPPER_RAIL);

        blockStateModelGenerator.registerSimpleCubeAll(CWBlocks.COPPER_CLOCK);

        blockStateModelGenerator.registerSimpleCubeAll(CWBlocks.RELAY);

        registerLever(blockStateModelGenerator);

        registerStickyBlock(blockStateModelGenerator, CWBlocks.STICKY_COPPER);
        registerStickyBlock(blockStateModelGenerator, CWBlocks.STICKY_COPPER_HONEY);

        registerCopperScaffolding(blockStateModelGenerator);

        registerCopperBarrel(blockStateModelGenerator);

        registerBattery(blockStateModelGenerator);

        registerBoundRelay(blockStateModelGenerator);
        registerPowered(blockStateModelGenerator, CWBlocks.RADIO_RELAY);
        registerPowered(blockStateModelGenerator, CWBlocks.ENDER_RELAY);
    }

    private static void registerPowered(BlockStateModelGenerator blockStateModelGenerator, Block block) {
        WeightedVariant defaultVariant = BlockStateModelGenerator.createWeightedVariant(
                TexturedModel.CUBE_ALL.upload(block, blockStateModelGenerator.modelCollector)
        );

        WeightedVariant onVariant = BlockStateModelGenerator.createWeightedVariant(
                blockStateModelGenerator.createSubModel(block, "_on", Models.CUBE_ALL, TextureMap::all)
        );

        blockStateModelGenerator.blockStateCollector
                .accept(
                        VariantsBlockModelDefinitionCreator
                                .of(block)
                                .with(
                                        BlockStateModelGenerator.createBooleanModelMap(
                                                Properties.POWERED,
                                                onVariant,
                                                defaultVariant
                                        )
                                )
                );
    }

    private static void registerBoundRelay(BlockStateModelGenerator blockStateModelGenerator) {
        Block boundRelay = CWBlocks.BOUND_RELAY;

        WeightedVariant defaultVariant = BlockStateModelGenerator.createWeightedVariant(
                TexturedModel.CUBE_ALL.upload(boundRelay, blockStateModelGenerator.modelCollector)
        );

        WeightedVariant onVariant = BlockStateModelGenerator.createWeightedVariant(
                blockStateModelGenerator.createSubModel(boundRelay, "_on", Models.CUBE_ALL, TextureMap::all)
        );

        blockStateModelGenerator.blockStateCollector
                .accept(
                        VariantsBlockModelDefinitionCreator
                                .of(boundRelay)
                                .with(createPowerModelMap(onVariant, defaultVariant))
                );
    }

    private static BlockStateVariantMap<WeightedVariant> createPowerModelMap(
            WeightedVariant nonZeroModel,
            WeightedVariant zeroModel
    ) {
        IntProperty property = Properties.POWER;

        BlockStateVariantMap.SingleProperty<WeightedVariant, Integer> map = BlockStateVariantMap.models(property);

        for (Integer integer : property.getValues()) {
            if (integer > 0) {
                map.register(integer, nonZeroModel);
            } else {
                map.register(integer, zeroModel);
            }
        }

        return map;
    }

    private static void registerRadio(ItemModelGenerator itemModelGenerator) {
        Item radio = CWItems.RADIO;
        ItemModel.Unbaked defaultModel = ItemModels.basic(itemModelGenerator.upload(radio, Models.GENERATED));
        ItemModel.Unbaked boundModel = ItemModels.basic(
                itemModelGenerator.registerSubModel(radio, "_bound", Models.GENERATED)
        );
        itemModelGenerator.registerCondition(radio, new BoundProperty(), boundModel, defaultModel);
    }

    private static void registerBattery(BlockStateModelGenerator blockStateModelGenerator) {
        Block battery = CWBlocks.BATTERY;

        WeightedVariant defaultVariant = BlockStateModelGenerator.createWeightedVariant(
                ModelIds.getBlockModelId(battery)
        );

        WeightedVariant onVariant = BlockStateModelGenerator.createWeightedVariant(
                ModelIds.getBlockSubModelId(battery, "_on")
        );

        blockStateModelGenerator.blockStateCollector
                .accept(
                        VariantsBlockModelDefinitionCreator
                                .of(battery)
                                .with(
                                        BlockStateModelGenerator.createBooleanModelMap(
                                                Properties.POWERED,
                                                onVariant,
                                                defaultVariant
                                        )
                                )
                                .apply(
                                        BlockStateVariantMap
                                                .operations(Properties.BLOCK_FACE, Properties.HORIZONTAL_FACING)
                                                .register(
                                                        BlockFace.CEILING,
                                                        Direction.NORTH,
                                                        BlockStateModelGenerator.ROTATE_X_180.then(
                                                                BlockStateModelGenerator.ROTATE_Y_180
                                                        )
                                                )
                                                .register(
                                                        BlockFace.CEILING,
                                                        Direction.EAST,
                                                        BlockStateModelGenerator.ROTATE_X_180.then(
                                                                BlockStateModelGenerator.ROTATE_Y_270
                                                        )
                                                )
                                                .register(
                                                        BlockFace.CEILING,
                                                        Direction.SOUTH,
                                                        BlockStateModelGenerator.ROTATE_X_180
                                                )
                                                .register(
                                                        BlockFace.CEILING,
                                                        Direction.WEST,
                                                        BlockStateModelGenerator.ROTATE_X_180.then(
                                                                BlockStateModelGenerator.ROTATE_Y_90
                                                        )
                                                )
                                                .register(
                                                        BlockFace.FLOOR,
                                                        Direction.NORTH,
                                                        BlockStateModelGenerator.NO_OP
                                                )
                                                .register(
                                                        BlockFace.FLOOR,
                                                        Direction.EAST,
                                                        BlockStateModelGenerator.ROTATE_Y_90
                                                )
                                                .register(
                                                        BlockFace.FLOOR,
                                                        Direction.SOUTH,
                                                        BlockStateModelGenerator.ROTATE_Y_180
                                                )
                                                .register(
                                                        BlockFace.FLOOR,
                                                        Direction.WEST,
                                                        BlockStateModelGenerator.ROTATE_Y_270
                                                )
                                                .register(
                                                        BlockFace.WALL,
                                                        Direction.NORTH,
                                                        BlockStateModelGenerator.ROTATE_X_90
                                                )
                                                .register(
                                                        BlockFace.WALL,
                                                        Direction.EAST,
                                                        BlockStateModelGenerator.ROTATE_X_90.then(
                                                                BlockStateModelGenerator.ROTATE_Y_90
                                                        )
                                                )
                                                .register(
                                                        BlockFace.WALL,
                                                        Direction.SOUTH,
                                                        BlockStateModelGenerator.ROTATE_X_90.then(
                                                                BlockStateModelGenerator.ROTATE_Y_180
                                                        )
                                                )
                                                .register(
                                                        BlockFace.WALL,
                                                        Direction.WEST,
                                                        BlockStateModelGenerator.ROTATE_X_90.then(
                                                                BlockStateModelGenerator.ROTATE_Y_270
                                                        )
                                                )
                                )
                );
    }

    private static void registerCopperBarrel(BlockStateModelGenerator blockStateModelGenerator) {
        Block barrel = CWBlocks.COPPER_BARREL;

        WeightedVariant weightedVariant = BlockStateModelGenerator.createWeightedVariant(
                TexturedModel.CUBE_BOTTOM_TOP.upload(barrel, blockStateModelGenerator.modelCollector)
        );

        blockStateModelGenerator.blockStateCollector
                .accept(
                        VariantsBlockModelDefinitionCreator
                                .of(barrel, weightedVariant)
                                .apply(BlockStateModelGenerator.UP_DEFAULT_ROTATION_OPERATIONS)
                );
    }

    private static void registerCopperScaffolding(BlockStateModelGenerator blockStateModelGenerator) {
        Block frame = CWBlocks.COPPER_SCAFFOLDING;

        blockStateModelGenerator.registerItemModel(frame.asItem());

        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockModelDefinitionCreator.of(
                        frame,
                        BlockStateModelGenerator.createWeightedVariant(ModelIds.getBlockModelId(frame))
                )
        );

    }

    private static void registerStickyBlock(BlockStateModelGenerator blockStateModelGenerator, Block block) {
        Identifier base = Copperworks.id("block/sticky_copper_base");

        blockStateModelGenerator.registerSingleton(
                block,
                TexturedModel.CUBE_TOP.andThen(textures -> {
                    textures.put(TextureKey.SIDE, base);
                    textures.put(TextureKey.BOTTOM, base);
                    textures.put(TextureKey.PARTICLE, base);
                })
        );
    }

    private static void registerLever(BlockStateModelGenerator blockStateModelGenerator) {
        Block lever = CWBlocks.COPPER_LEVER;

        WeightedVariant weightedVariant = BlockStateModelGenerator.createWeightedVariant(
                ModelIds.getBlockModelId(lever)
        );

        WeightedVariant weightedVariantOn = BlockStateModelGenerator.createWeightedVariant(
                ModelIds.getBlockSubModelId(lever, "_on")
        );

        blockStateModelGenerator.registerItemModel(lever);

        blockStateModelGenerator
                .blockStateCollector
                .accept(
                        VariantsBlockModelDefinitionCreator
                                .of(lever)
                                .with(
                                        BlockStateModelGenerator.createBooleanModelMap(
                                                Properties.POWERED,
                                                weightedVariant,
                                                weightedVariantOn
                                        )
                                )
                                .apply(
                                        BlockStateVariantMap
                                                .operations(
                                                        Properties.BLOCK_FACE,
                                                        Properties.HORIZONTAL_FACING
                                                )
                                                .register(
                                                        BlockFace.CEILING,
                                                        Direction.NORTH,
                                                        BlockStateModelGenerator.ROTATE_X_180.then(
                                                                BlockStateModelGenerator.ROTATE_Y_180
                                                        )
                                                )
                                                .register(
                                                        BlockFace.CEILING,
                                                        Direction.EAST,
                                                        BlockStateModelGenerator.ROTATE_X_180.then(
                                                                BlockStateModelGenerator.ROTATE_Y_270
                                                        )
                                                )
                                                .register(
                                                        BlockFace.CEILING,
                                                        Direction.SOUTH,
                                                        BlockStateModelGenerator.ROTATE_X_180
                                                )
                                                .register(
                                                        BlockFace.CEILING,
                                                        Direction.WEST,
                                                        BlockStateModelGenerator.ROTATE_X_180.then(
                                                                BlockStateModelGenerator.ROTATE_Y_90
                                                        )
                                                )
                                                .register(
                                                        BlockFace.FLOOR,
                                                        Direction.NORTH,
                                                        BlockStateModelGenerator.NO_OP
                                                )
                                                .register(
                                                        BlockFace.FLOOR,
                                                        Direction.EAST,
                                                        BlockStateModelGenerator.ROTATE_Y_90
                                                )
                                                .register(
                                                        BlockFace.FLOOR,
                                                        Direction.SOUTH,
                                                        BlockStateModelGenerator.ROTATE_Y_180
                                                )
                                                .register(
                                                        BlockFace.FLOOR,
                                                        Direction.WEST,
                                                        BlockStateModelGenerator.ROTATE_Y_270
                                                )
                                                .register(
                                                        BlockFace.WALL,
                                                        Direction.NORTH,
                                                        BlockStateModelGenerator.ROTATE_X_90
                                                )
                                                .register(
                                                        BlockFace.WALL,
                                                        Direction.EAST,
                                                        BlockStateModelGenerator.ROTATE_X_90.then(
                                                                BlockStateModelGenerator.ROTATE_Y_90
                                                        )
                                                )
                                                .register(
                                                        BlockFace.WALL,
                                                        Direction.SOUTH,
                                                        BlockStateModelGenerator.ROTATE_X_90.then(
                                                                BlockStateModelGenerator.ROTATE_Y_180
                                                        )
                                                )
                                                .register(
                                                        BlockFace.WALL,
                                                        Direction.WEST,
                                                        BlockStateModelGenerator.ROTATE_X_90.then(
                                                                BlockStateModelGenerator.ROTATE_Y_270
                                                        )
                                                )
                                )
                );
    }
}
