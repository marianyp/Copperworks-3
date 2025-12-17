package dev.mariany.copperworks.event.entity;

import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.Map;

public class MinecartEventHandler {
    private static final Map<Block, FastRailProperties> FAST_RAILS = Map.of(
            CWBlocks.WOODEN_RAIL,
            new FastRailProperties(0.4F, 0.3F)
    );

    private static final Map<Block, Float> FRAGILE_RAILS = Map.of(CWBlocks.WOODEN_RAIL, 0.03F);

    public static void onMinecartTravel(AbstractMinecartEntity abstractMinecart) {
        if (!abstractMinecart.getWorld().isClient()) {
            BlockPos previousPosition = abstractMinecart.getBlockPos();

            handleFastRails(abstractMinecart);
            handleFragileRails(abstractMinecart, previousPosition);
        }
    }

    private static void handleFastRails(AbstractMinecartEntity abstractMinecart) {
        World world = abstractMinecart.getWorld();
        BlockPos railPos = abstractMinecart.getBlockPos();
        BlockState railBlockState = world.getBlockState(railPos);
        Block railBlock = railBlockState.getBlock();
        Entity passenger = abstractMinecart.getFirstPassenger();
        FastRailProperties fastRailProperties = FAST_RAILS.get(railBlock);

        if (fastRailProperties != null && passenger != null && !isCurvedRail(railBlockState)) {
            boolean shouldMove;

            if (passenger instanceof PlayerEntity player) {
                HungerManager hungerManager = player.getHungerManager();
                shouldMove = hungerManager.getFoodLevel() > 6;

                if (shouldMove && player.age % 10 == 0) {
                    hungerManager.addExhaustion(fastRailProperties.exhaustion);
                }
            } else {
                shouldMove = false;
            }

            if (shouldMove) {
                moveMinecart(abstractMinecart, fastRailProperties.additionalSpeed);
            }
        }
    }

    private static boolean isCurvedRail(BlockState state) {
        RailShape shape = state.get(Properties.RAIL_SHAPE);

        return shape == RailShape.NORTH_EAST || shape == RailShape.NORTH_WEST || shape == RailShape.SOUTH_EAST ||
                shape == RailShape.SOUTH_WEST;
    }

    private static void moveMinecart(AbstractMinecartEntity minecart, float speed) {
        Vec3d currentVelocity = minecart.getVelocity();
        Vec3d newVelocity = currentVelocity.normalize().multiply(speed);
        minecart.setVelocity(newVelocity);
        minecart.velocityModified = true;
    }

    private static void handleFragileRails(AbstractMinecartEntity abstractMinecart, BlockPos previousPosition) {
        World world = abstractMinecart.getWorld();
        Random random = world.getRandom();
        BlockState railBlockState = world.getBlockState(previousPosition);
        Block railBlock = railBlockState.getBlock();
        Entity passenger = abstractMinecart.getFirstPassenger();
        Float breakChance = FRAGILE_RAILS.get(railBlock);

        if (breakChance != null && passenger != null) {
            Vec3d currentVelocity = abstractMinecart.getVelocity();
            double currentSpeed = currentVelocity.length();

            if (currentSpeed > 0 && passenger.age % 20 == 0) {
                if (random.nextFloat() < breakChance) {
                    breakWoodenRail(world, previousPosition);
                }
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

    record FastRailProperties(float additionalSpeed, float exhaustion) {
    }
}
