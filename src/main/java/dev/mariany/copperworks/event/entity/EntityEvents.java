package dev.mariany.copperworks.event.entity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;

public class EntityEvents {
    private EntityEvents() {
    }

    public static final Event<BeforeMinecartTravel> BEFORE_MINECART_TRAVEL = EventFactory.createArrayBacked(
            BeforeMinecartTravel.class,
            callbacks -> minecartEntity -> {
                for (BeforeMinecartTravel callback : callbacks) {
                    callback.onMinecartTravel(minecartEntity);
                }
            }
    );

    public static final Event<BeforePotionCollision> BEFORE_POTION_COLLISION = EventFactory.createArrayBacked(
            BeforePotionCollision.class,
            callbacks -> potionEntity -> {
                for (BeforePotionCollision callback : callbacks) {
                    callback.onPotionCollision(potionEntity);
                }
            }
    );

    public interface BeforeMinecartTravel {
        void onMinecartTravel(AbstractMinecartEntity abstractMinecart);
    }

    public interface BeforePotionCollision {
        void onPotionCollision(PotionEntity potionEntity);
    }
}
