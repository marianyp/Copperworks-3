package dev.mariany.copperworks.event.block;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public final class BlockEvents {
    public static final Event<OverrideBlockInteraction> OVERRIDE_BLOCK_INTERACTION = EventFactory.createArrayBacked(
            OverrideBlockInteraction.class,
            callbacks -> (player, hand, hitResult) -> {
                for (OverrideBlockInteraction callback : callbacks) {
                    if (callback.shouldOverrideBlockInteraction(player, hand, hitResult)) {
                        return true;
                    }
                }

                return false;
            }
    );

    private BlockEvents() {
    }

    public interface OverrideBlockInteraction {
        boolean shouldOverrideBlockInteraction(PlayerEntity player, Hand hand, BlockHitResult hitResult);
    }
}
