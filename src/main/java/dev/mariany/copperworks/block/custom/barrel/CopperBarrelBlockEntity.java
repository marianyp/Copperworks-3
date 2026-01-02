package dev.mariany.copperworks.block.custom.barrel;

import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.custom.InventoryNetworkBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class CopperBarrelBlockEntity extends InventoryNetworkBlockEntity {
    public CopperBarrelBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(CWBlockEntities.COPPER_BARREL, blockPos, blockState);
    }

    protected CopperBarrelBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(
                blockEntityType,
                pos,
                state,
                24,
                Text.translatable("container.copperworks.copper_barrel")
        );
    }
}
