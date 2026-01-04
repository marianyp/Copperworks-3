package dev.mariany.copperworks.event.block;

import dev.mariany.copperworks.item.custom.PartialDragonBreathItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class UseBlockHandler {
    public static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult) {
        return PartialDragonBreathItem.glassBottleFill(player, world, hand, blockHitResult);
    }
}
