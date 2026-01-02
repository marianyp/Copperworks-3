package dev.mariany.copperworks.block;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.custom.barrel.CopperBarrelBlockEntity;
import dev.mariany.copperworks.block.custom.clock.ClockBlockEntity;
import dev.mariany.copperworks.block.custom.relay.BoundRelayBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class CWBlockEntities {
    public static final BlockEntityType<CopperBarrelBlockEntity> COPPER_BARREL = register(
            "copper_barrel",
            FabricBlockEntityTypeBuilder.create(CopperBarrelBlockEntity::new, CWBlocks.COPPER_BARREL).build()
    );

    public static final BlockEntityType<ClockBlockEntity> CLOCK = register(
            "copper_clock",
            FabricBlockEntityTypeBuilder.create(ClockBlockEntity::new, CWBlocks.COPPER_CLOCK).build()
    );

    public static final BlockEntityType<BoundRelayBlockEntity> BOUND_RELAY = register(
            "bound_relay",
            FabricBlockEntityTypeBuilder.create(BoundRelayBlockEntity::new, CWBlocks.BOUND_RELAY).build()
    );

    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Copperworks.id(path), blockEntityType);
    }

    public static void bootstrap() {
        Copperworks.LOGGER.info("Registering Block Entities for {}", Copperworks.MOD_ID);
    }
}
