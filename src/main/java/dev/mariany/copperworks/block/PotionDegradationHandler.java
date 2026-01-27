package dev.mariany.copperworks.block;

import dev.mariany.copperworks.gamerule.CWGamerules;
import net.minecraft.block.BlockState;
import net.minecraft.block.Degradable;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Optional;

public final class PotionDegradationHandler {
    private PotionDegradationHandler() {
    }

    public static void onPotionCollision(PotionEntity potionEntity) {
        if (potionEntity.getEntityWorld() instanceof ServerWorld world) {
            if (!world.getGameRules().getBoolean(CWGamerules.WATER_POTIONS_DEGRADE_BLOCKS)) {
                return;
            }

            ItemStack stack = potionEntity.getStack();
            PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);

            if (potionContentsComponent == null) {
                return;
            }

            Optional<RegistryEntry<Potion>> optionalPotionEntry = potionContentsComponent.potion();

            if (optionalPotionEntry.isEmpty()) {
                return;
            }

            RegistryEntry<Potion> potionEntry = optionalPotionEntry.get();

            if (potionEntry.value().getEffects().isEmpty()) {
                degradeArea(potionEntity);
            }
        }
    }

    private static void degradeArea(PotionEntity potionEntity) {
        World world = potionEntity.getEntityWorld();
        Box box = potionEntity.getBoundingBox().expand(2, 0.5, 2);

        BlockPos.iterate(box).forEach(pos -> {
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof Degradable<?> degradable) {
                degradable.getDegradationResult(state)
                          .ifPresent(result -> world.setBlockState(
                                  pos,
                                  result.getBlock().getStateWithProperties(state)
                          ));
            }
        });
    }
}
