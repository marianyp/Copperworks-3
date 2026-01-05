package dev.mariany.copperworks.block;

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

public interface DynamicRails {
    Map<Block, RailProperties> MOMENTUM_RAILS = Map.of(
            CWBlocks.WOODEN_RAIL, new RailProperties(0.3F, 0.3F),
            CWBlocks.COPPER_RAIL, new RailProperties(0.4F, 0.1F)
    );

    Map<Block, Float> FRAGILE_RAILS = Map.of(CWBlocks.WOODEN_RAIL, 0.03F);

    static void onMinecartTravel(AbstractMinecartEntity abstractMinecart) {
        if (!abstractMinecart.getEntityWorld().isClient()) {
            BlockPos previousPosition = abstractMinecart.getBlockPos();

            handleMomentumRails(abstractMinecart);
            handleFragileRails(abstractMinecart, previousPosition);
        }
    }

    private static void handleMomentumRails(AbstractMinecartEntity abstractMinecart) {
        World world = abstractMinecart.getEntityWorld();
        BlockPos railPos = abstractMinecart.getBlockPos();
        BlockState railBlockState = world.getBlockState(railPos);
        Block railBlock = railBlockState.getBlock();
        Entity passenger = abstractMinecart.getFirstPassenger();
        RailProperties railProperties = MOMENTUM_RAILS.get(railBlock);

        if (railProperties != null && passenger != null && !isCurvedRail(railBlockState)) {
            boolean shouldMove;

            if (passenger instanceof PlayerEntity player) {
                HungerManager hungerManager = player.getHungerManager();
                shouldMove = hungerManager.getFoodLevel() > 6;

                if (shouldMove && player.age % 10 == 0) {
                    hungerManager.addExhaustion(railProperties.exhaustion);
                }
            } else {
                shouldMove = false;
            }

            if (shouldMove) {
                moveMinecart(abstractMinecart, railProperties.additionalSpeed);
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
        World world = abstractMinecart.getEntityWorld();
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

    record RailProperties(float additionalSpeed, float exhaustion) {
    }
}
