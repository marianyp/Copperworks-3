package dev.mariany.copperworks.event.entity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;

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

    public interface BeforeMinecartTravel {
        void onMinecartTravel(AbstractMinecartEntity abstractMinecart);
    }
}
