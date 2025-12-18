package dev.mariany.copperworks.item;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.advancement.criterion.CWCriterion;
import dev.mariany.copperworks.block.CWBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.function.Consumer;

public class CopperUpgradeItem extends Item {
    private static final Text APPLIES_TO_TEXT =
            Text.translatable(
                        Util.createTranslationKey(
                                "item",
                                Identifier.ofVanilla("smithing_template.applies_to")
                        )
                )
                .formatted(Formatting.GRAY);

    private static final Text COPPER_UPGRADE_APPLIES_TO_TEXT = Text
            .translatable(
                    Util.createTranslationKey("item", Copperworks.id("copper_upgrade_kit.applies_to"))
            )
            .formatted(Formatting.BLUE);

    public CopperUpgradeItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplayComponent displayComponent,
            Consumer<Text> textConsumer,
            TooltipType type
    ) {
        textConsumer.accept(APPLIES_TO_TEXT);
        textConsumer.accept(ScreenTexts.space().append(COPPER_UPGRADE_APPLIES_TO_TEXT));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        ItemStack itemStack = context.getStack();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.getBlock().equals(CWBlocks.WOODEN_RAIL)) {
            if (player != null) {
                EquipmentSlot slot = LivingEntity.getSlotForHand(hand);
                itemStack.damage(1, player, slot);
            }

            BlockState updatedBlockState = CWBlocks.COPPER_RAIL.getDefaultState();

            for (Property<?> property : blockState.getProperties()) {
                updatedBlockState = updatedBlockState.withIfExists((Property) property, blockState.get(property));
            }

            world.setBlockState(blockPos, updatedBlockState);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(player, updatedBlockState));

            world.playSound(
                    player,
                    blockPos.getX(),
                    blockPos.getY(),
                    blockPos.getZ(),
                    SoundEvents.BLOCK_COPPER_PLACE,
                    SoundCategory.NEUTRAL,
                    0.33F,
                    MathHelper.nextBetween(world.random, 0.7F, 1F),
                    world.random.nextLong()
            );

            if (player instanceof ServerPlayerEntity serverPlayer) {
                CWCriterion.UPGRADE_WOODEN_RAIL.trigger(serverPlayer);
            }

            return ActionResult.SUCCESS;
        }

        return super.useOnBlock(context);
    }
}
