package dev.mariany.copperworks.world.poi;

import com.google.common.collect.ImmutableSet;
import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.CWBlocks;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Set;

public class CWPointOfInterestTypes {
    public static final RegistryKey<PointOfInterestType> MUFFLER = of(
            "muffler",
            1,
            1,
            CWBlocks.MUFFLER
    );

    private CWPointOfInterestTypes() {}

    private static RegistryKey<PointOfInterestType> of(
            String name,
            int ticketCount,
            int searchDistance,
            Block... blocks
    ) {
        Identifier id = Copperworks.id(name);

        for (Block block : blocks) {
            PointOfInterestHelper.register(id, ticketCount, searchDistance, getStatesOfBlock(block));
        }

        return RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, id);
    }

    private static Set<BlockState> getStatesOfBlock(Block block) {
        return ImmutableSet.copyOf(block.getStateManager().getStates());
    }

    public static void bootstrap() {
        Copperworks.bootstrapLog("Point of Interest Types");
    }
}
