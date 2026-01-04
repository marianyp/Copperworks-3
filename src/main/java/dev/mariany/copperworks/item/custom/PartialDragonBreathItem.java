package dev.mariany.copperworks.item.custom;

import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.item.CWItems;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CryingObsidianBlock;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PartialDragonBreathItem extends Item {
    private final int maxFullness;

    public PartialDragonBreathItem(Settings settings, int maxFullness) {
        super(settings);
        this.maxFullness = maxFullness;
    }

    public static float getAmountFilled(ItemStack stack) {
        return stack.getOrDefault(CWComponents.DRAGON_BREATH_FULLNESS, 0);
    }

    public static ActionResult glassBottleFill(
            PlayerEntity player,
            World world,
            Hand hand,
            BlockHitResult blockHitResult
    ) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem() instanceof GlassBottleItem) {
            BlockPos pos = blockHitResult.getBlockPos();

            if (world.getBlockState(pos).isOf(Blocks.CRYING_OBSIDIAN)) {
                playFillSound(world, player);

                stack.decrement(1);

                world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);

                ItemStack newStack = CWItems.PARTIAL_DRAGON_BREATH.getDefaultStack();

                if (stack.isEmpty()) {
                    player.setStackInHand(hand, newStack);
                } else if (!player.giveItemStack(newStack)) {
                    ItemEntity itemEntity = player.dropItem(newStack, true);

                    if (itemEntity != null) {
                        itemEntity.resetPickupDelay();
                        itemEntity.setOwner(player.getUuid());
                    }
                }

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    public int getMaxFullness() {
        return this.maxFullness;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        List<AreaEffectCloudEntity> areaEffectClouds = world.getEntitiesByClass(
                AreaEffectCloudEntity.class,
                player.getBoundingBox().expand(2),
                PartialDragonBreathItem::isAreaEffectCloud
        );

        ItemStack stack = player.getStackInHand(hand);

        if (!areaEffectClouds.isEmpty()) {
            AreaEffectCloudEntity areaEffectCloudEntity = areaEffectClouds.getFirst();
            areaEffectCloudEntity.setRadius(areaEffectCloudEntity.getRadius() - 0.5F);

            playFillSound(world, player);

            world.emitGameEvent(player, GameEvent.FLUID_PICKUP, player.getEntityPos());

            if (player instanceof ServerPlayerEntity serverPlayerEntity) {
                Criteria.PLAYER_INTERACTED_WITH_ENTITY.trigger(serverPlayerEntity, stack, areaEffectCloudEntity);
            }

            player.incrementStat(Stats.USED.getOrCreateStat(this));

            ItemStack newStack = ItemUsage.exchangeStack(stack, player, Items.DRAGON_BREATH.getDefaultStack());

            return ActionResult.SUCCESS.withNewHandStack(newStack);
        }

        return ActionResult.PASS;
    }

    private static boolean isAreaEffectCloud(@Nullable Entity entity) {
        if (entity instanceof AreaEffectCloudEntity areaEffectCloud) {
            return areaEffectCloud.isAlive() && areaEffectCloud.getOwner() instanceof EnderDragonEntity;
        }

        return false;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        ItemStack stack = context.getStack();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.getBlock() instanceof CryingObsidianBlock) {
            if (player != null && world.canEntityModifyAt(player, blockPos)) {
                fill(stack, player, hand);

                player.incrementStat(Stats.USED.getOrCreateStat(this));

                playFillSound(world, player);

                world.setBlockState(blockPos, Blocks.OBSIDIAN.getDefaultState());

                world.emitGameEvent(player, GameEvent.FLUID_PICKUP, player.getEntityPos());
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    private void fill(ItemStack stack, PlayerEntity player, Hand hand) {
        ItemStack existingStack = stack.copy();
        int dragonBreathFill = existingStack.getOrDefault(CWComponents.DRAGON_BREATH_FULLNESS, 1);

        ItemStack newStack;

        if (dragonBreathFill < this.maxFullness - 1) {
            newStack = existingStack.copyWithCount(1);
            newStack.set(CWComponents.DRAGON_BREATH_FULLNESS, dragonBreathFill + 1);
        } else {
            newStack = Items.DRAGON_BREATH.getDefaultStack();
        }

        existingStack.decrement(1);

        if (existingStack.isEmpty()) {
            player.setStackInHand(hand, newStack);
        } else {
            player.setStackInHand(hand, existingStack);

            if (!player.giveItemStack(newStack)) {
                ItemEntity itemEntity = player.dropItem(newStack, true);

                if (itemEntity != null) {
                    itemEntity.resetPickupDelay();
                    itemEntity.setOwner(player.getUuid());
                }
            }
        }
    }

    private static void playFillSound(World world, PlayerEntity player) {
        world.playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH,
                SoundCategory.NEUTRAL
        );
    }
}

