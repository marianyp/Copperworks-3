package dev.mariany.copperworks.datagen;

import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.item.CWItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.client.data.*;
import net.minecraft.client.render.model.json.WeightedVariant;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class CWModelProvider extends FabricModelProvider {
    public CWModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(CWItems.COPPER_UPGRADE_KIT, Models.GENERATED);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerTurnableRail(CWBlocks.WOODEN_RAIL);
        blockStateModelGenerator.registerTurnableRail(CWBlocks.COPPER_RAIL);
        this.registerLever(blockStateModelGenerator, CWBlocks.COPPER_LEVER);
    }

    private void registerLever(BlockStateModelGenerator blockStateModelGenerator, Block lever) {
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
                                .coordinate(
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
