package dev.mariany.copperworks.item.upgrade.copper;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.advancement.criterion.CWCriterion;
import dev.mariany.copperworks.inventory.InventoryHelper;
import dev.mariany.copperworks.registry.CWRegistryKeys;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    public CopperUpgradeItem(Settings settings) {
        super(settings);
    }

    public static boolean shouldOverrideInteraction(PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        World world = player.getEntityWorld();
        DynamicRegistryManager registryManager = world.getRegistryManager();

        if (player.getStackInHand(hand).getItem() instanceof CopperUpgradeItem) {
            BlockState state = world.getBlockState(hitResult.getBlockPos());

            Optional<Registry<CopperUpgrade>> optionalRegistry = registryManager.getOptional(
                    CWRegistryKeys.COPPER_UPGRADE
            );

            return state.getRegistryEntry()
                        .getKey()
                        .map(key -> optionalRegistry
                                .map(registry -> registry.getEntry(key.getValue()).isPresent())
                                .orElse(false)
                        )
                        .orElse(false);
        }

        return false;
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

        RegistryWrapper.WrapperLookup wrapperLookup = context.getRegistryLookup();

        if (wrapperLookup != null) {
            wrapperLookup
                    .getOptional(CWRegistryKeys.COPPER_UPGRADE)
                    .ifPresent(upgradeRegistry -> upgradeRegistry
                            .streamEntries()
                            .forEach(upgradeReference -> {
                                Optional<RegistryKey<CopperUpgrade>> optionalCopperUpgradeRegistryKey =
                                        upgradeReference.getKey();

                                optionalCopperUpgradeRegistryKey
                                        .flatMap(upgradeRegistryKey -> wrapperLookup
                                                .getOptional(RegistryKeys.BLOCK)
                                                .flatMap(blockRegistry -> blockRegistry
                                                        .getOptional(
                                                                RegistryKey.of(
                                                                        RegistryKeys.BLOCK,
                                                                        upgradeRegistryKey.getValue()
                                                                )
                                                        )
                                                )
                                        )
                                        .ifPresent(blockReference -> {
                                            String translationKey = Util.createTranslationKey(
                                                    "item",
                                                    Copperworks.id("copper_upgrade_kit.applies_to")
                                            );

                                            MutableText mutableText =
                                                    Text.translatable(
                                                                translationKey,
                                                                blockReference.value().getName()
                                                        )
                                                        .formatted(Formatting.BLUE);

                                            textConsumer.accept(ScreenTexts.space().append(mutableText));
                                        });
                            })
                    );
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        ItemStack itemStack = context.getStack();
        BlockPos pos = context.getBlockPos();
        BlockState blockState = world.getBlockState(pos);

        Optional<RegistryKey<Block>> optionalBlockKey = blockState.getRegistryEntry().getKey();

        DynamicRegistryManager dynamicRegistryManager = world.getRegistryManager();

        Optional<Registry<CopperUpgrade>> optionalCopperUpgradesRegistry = dynamicRegistryManager.getOptional(
                CWRegistryKeys.COPPER_UPGRADE
        );

        if (optionalBlockKey.isPresent() && optionalCopperUpgradesRegistry.isPresent()) {
            RegistryKey<Block> blockKey = optionalBlockKey.get();
            Registry<CopperUpgrade> copperUpgradesRegistry = optionalCopperUpgradesRegistry.get();

            CopperUpgrade copperUpgrade = copperUpgradesRegistry.get(blockKey.getValue());

            if (copperUpgrade != null) {
                Block convertsTo = copperUpgrade.to();

                if (player != null) {
                    itemStack.damage(1, player, hand.getEquipmentSlot());
                }

                BlockState updatedBlockState = copyProperties(
                        convertsTo.getDefaultState(),
                        blockState,
                        copperUpgrade.copiedProperties()
                );

                boolean mergeInventories = copperUpgrade.mergeInventories() && !world.isClient();

                List<ItemStack> stacks = InventoryHelper.copy(world, pos, mergeInventories);

                world.setBlockState(pos, updatedBlockState);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, updatedBlockState));

                if (mergeInventories) {
                    List<ItemStack> overflow = InventoryHelper.addAll(world, pos, stacks);
                    InventoryHelper.scatterItems(world, pos, overflow);
                }

                if (world.isClient()) {
                    world.syncWorldEvent(
                            WorldEvents.BLOCK_BROKEN,
                            pos,
                            Block.getRawIdFromState(updatedBlockState)
                    );
                }

                if (player instanceof ServerPlayerEntity serverPlayer) {
                    CWCriterion.USE_COPPER_UPGRADE_KIT.trigger(serverPlayer);
                }

                return ActionResult.SUCCESS;
            }
        }

        return super.useOnBlock(context);
    }

    private BlockState copyProperties(
            BlockState originalState,
            BlockState sourceState,
            Collection<String> propertyNames
    ) {
        BlockState updatedBlockState = originalState;

        for (Property<?> property : sourceState.getProperties()) {
            if (propertyNames.contains(property.getName())) {
                updatedBlockState = updatedBlockState.withIfExists((Property) property, sourceState.get(property));
            }
        }

        return updatedBlockState;
    }
}
