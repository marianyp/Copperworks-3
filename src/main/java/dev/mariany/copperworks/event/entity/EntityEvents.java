package dev.mariany.copperworks.event.entity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class EntityEvents {
    private EntityEvents() {
    }

    public static final Event<BeforeMinecartTravel> BEFORE_MINECART_TRAVEL = EventFactory.createArrayBacked(
            BeforeMinecartTravel.class,
            callbacks -> map -> {
                for (BeforeMinecartTravel callback : callbacks) {
                    callback.onMinecartTravel(map);
                }
            }
    );

    public static final Event<BeforePotionCollision> BEFORE_POTION_COLLISION = EventFactory.createArrayBacked(
            BeforePotionCollision.class,
            callbacks -> (
                    world,
                    origin,
                    potionContentsComponent,
                    radius
            ) -> {
                for (BeforePotionCollision callback : callbacks) {
                    callback.onPotionCollision(world, origin, potionContentsComponent, radius);
                }
            }
    );

    public interface BeforeMinecartTravel {
        void onMinecartTravel(AbstractMinecartEntity abstractMinecart);
    }

    public interface BeforePotionCollision {
        void onPotionCollision(
                ServerWorld world,
                Vec3d origin,
                PotionContentsComponent potionContentsComponent,
                float radius
        );
    }
}
