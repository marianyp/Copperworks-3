package dev.mariany.copperworks.block.entity;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.CWBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class CWBlockEntities {
    public static final BlockEntityType<CopperBarrelBlockEntity> COPPER_BARREL = register(
            "copper_barrel",
            FabricBlockEntityTypeBuilder.create(CopperBarrelBlockEntity::new, CWBlocks.COPPER_BARREL).build()
    );

    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Copperworks.id(path), blockEntityType);
    }

    public static void bootstrap() {
        Copperworks.LOGGER.info("Registering Block Entities for {}", Copperworks.MOD_ID);
    }
}
