package dev.mariany.copperworks.event.server;

import dev.mariany.copperworks.block.StickyHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;

import java.util.List;

public class ServerTickEventsHandler {
    public static void onWorldTick(ServerWorld world) {
        List<? extends LivingEntity> livingEntities = world.getEntitiesByType(
                TypeFilter.instanceOf(LivingEntity.class),
                livingEntity -> !livingEntity.isRemoved()
        );

        for (LivingEntity livingEntity : livingEntities) {
            onLivingEntityTick(livingEntity);
        }
    }

    private static void onLivingEntityTick(LivingEntity livingEntity) {
        StickyHandler.onLivingEntityTick(livingEntity);
    }
}
