package dev.mariany.copperworks.degrade;

import dev.mariany.copperworks.gamerule.CWGamerules;
import net.minecraft.block.BlockState;
import net.minecraft.block.Degradable;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public interface PotionDegradationHandler {
    static void onPotionCollision(
            ServerWorld world,
            Vec3d origin,
            PotionContentsComponent potionContentsComponent,
            float radius
    ) {
        if (!world.getGameRules().getBoolean(CWGamerules.WATER_POTIONS_DEGRADE_BLOCKS)) {
            return;
        }

        Optional<RegistryEntry<Potion>> optionalPotionEntry = potionContentsComponent.potion();

        if (optionalPotionEntry.isEmpty()) {
            return;
        }

        RegistryEntry<Potion> potionEntry = optionalPotionEntry.get();

        if (potionEntry.value().getEffects().isEmpty()) {
            degradeArea(world, origin, radius);
        }
    }

    private static void degradeArea(ServerWorld world, Vec3d origin, float radius) {
        float halfRadius = radius / 2;
        Box box = Box.from(origin.offset(Direction.DOWN, 0.5)).expand(halfRadius, 0, halfRadius);

        BlockPos.iterate(box).forEach(pos -> {
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof Degradable<?> degradable) {
                degradable.getDegradationResult(state).ifPresent(
                        degradationResult -> world.setBlockState(
                                pos,
                                degradationResult.getBlock().getStateWithProperties(state)
                        )
                );
            }
        });
    }
}
