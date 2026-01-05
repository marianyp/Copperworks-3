package dev.mariany.copperworks.block;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.custom.barrel.CopperBarrelBlockEntity;
import dev.mariany.copperworks.block.custom.clock.ClockBlockEntity;
import dev.mariany.copperworks.block.custom.relay.bound.BoundRelayBlockEntity;
import dev.mariany.copperworks.block.custom.relay.ender.EnderRelayBlockEntity;
import dev.mariany.copperworks.block.custom.relay.radio.RadioRelayBlockEntity;
import dev.mariany.copperworks.block.custom.sensor.SensorBlockEntity;
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

    public static final BlockEntityType<RadioRelayBlockEntity> RADIO_RELAY = register(
            "radio_relay",
            FabricBlockEntityTypeBuilder.create(RadioRelayBlockEntity::new, CWBlocks.RADIO_RELAY).build()
    );

    public static final BlockEntityType<EnderRelayBlockEntity> ENDER_RELAY = register(
            "ender_relay",
            FabricBlockEntityTypeBuilder.create(EnderRelayBlockEntity::new, CWBlocks.ENDER_RELAY).build()
    );

    public static final BlockEntityType<SensorBlockEntity> SENSOR = register(
            "sensor",
            FabricBlockEntityTypeBuilder.create(SensorBlockEntity::new, CWBlocks.COPPER_SENSOR).build()
    );

    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Copperworks.id(path), blockEntityType);
    }

    public static void bootstrap() {
        Copperworks.bootstrapLog("Block Entities");
    }
}
