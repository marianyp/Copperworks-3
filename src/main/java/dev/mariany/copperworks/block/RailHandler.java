package dev.mariany.copperworks.block;

import dev.mariany.copperworks.block.custom.rail.FragileRail;
import dev.mariany.copperworks.block.custom.rail.SpeedRail;
import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class RailHandler {
    private RailHandler() {
    }

    public static void onMinecartTravel(AbstractMinecartEntity minecart) {
        if (!minecart.getEntityWorld().isClient()) {
            BlockPos previousPosition = minecart.getBlockPos();
            boolean moving = minecart.getVelocity().lengthSquared() > 0.01;

            handleMomentumRails(minecart);
            handleFragileRails(minecart, previousPosition, moving);
        }
    }

    private static void handleMomentumRails(AbstractMinecartEntity abstractMinecart) {
        World world = abstractMinecart.getEntityWorld();
        BlockPos pos = abstractMinecart.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof SpeedRail speedRail) {
            if (!isCurvedRail(state)) {
                if (abstractMinecart.getFirstPassenger() instanceof PlayerEntity player) {
                    HungerManager hungerManager = player.getHungerManager();

                    if (hungerManager.getFoodLevel() <= 6) {
                        return;
                    }

                    if (player.age % 10 == 0) {
                        hungerManager.addExhaustion(speedRail.getExhaustion());
                    }
                }

                moveMinecart(abstractMinecart, speedRail.getSpeed());
            }
        }
    }

    private static boolean isCurvedRail(BlockState state) {
        RailShape shape = state.get(Properties.RAIL_SHAPE);

        return shape == RailShape.NORTH_EAST || shape == RailShape.NORTH_WEST || shape == RailShape.SOUTH_EAST ||
                shape == RailShape.SOUTH_WEST;
    }

    private static void moveMinecart(AbstractMinecartEntity minecart, float speed) {
        minecart.setVelocity(minecart.getVelocity().normalize().multiply(minecart.getFinalGravity() + speed));
        minecart.velocityModified = true;
    }

    private static void handleFragileRails(AbstractMinecartEntity minecart, BlockPos previousPosition, boolean moving) {
        World world = minecart.getEntityWorld();
        Random random = world.getRandom();
        BlockState state = world.getBlockState(previousPosition);

        if (moving && minecart.hasPassengers() && state.getBlock() instanceof FragileRail fragileRail) {
            if (minecart.age % 20 == 0 && random.nextFloat() < fragileRail.getBreakChance()) {
                breakWoodenRail(world, previousPosition);
            }
        }
    }

    private static void breakWoodenRail(World world, BlockPos blockPos) {
        world.playSound(
                null,
                blockPos,
                CWSoundEvents.BLOCK_WOODEN_RAIL_BREAK,
                SoundCategory.BLOCKS,
                0.1F,
                1
        );

        world.breakBlock(blockPos, false);
    }
}
