package dev.mariany.copperworks.item.custom.copperupgrade;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.advancement.criterion.CWCriterion;
import dev.mariany.copperworks.inventory.InventoryHelper;
import dev.mariany.copperworks.registry.CWRegistryKeys;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

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
    @SuppressWarnings("deprecation")
    public void appendTooltip(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplayComponent displayComponent,
            Consumer<Text> textConsumer,
            TooltipType type
    ) {
        textConsumer.accept(APPLIES_TO_TEXT);

        textConsumer.accept(getAppliesToText(Items.NETHERITE_BOOTS.getName()));

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
                                        .ifPresent(blockReference -> textConsumer.accept(
                                                getAppliesToText(blockReference.value().getName())
                                        ));
                            })
                    );
        }
    }

    private static Text getAppliesToText(Text text) {
        String translationKey = Util.createTranslationKey(
                "item",
                Copperworks.id("copper_upgrade_kit.applies_to")
        );

        return ScreenTexts.space().append(Text.translatable(translationKey, text).formatted(Formatting.BLUE));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        DynamicRegistryManager dynamicRegistryManager = world.getRegistryManager();

        return CopperUpgrades.getCopperUpgrade(dynamicRegistryManager, state)
                             .map(copperUpgrade -> applyCopperUpgrade(context, state, copperUpgrade))
                             .orElse(ActionResult.PASS);
    }

    protected static ActionResult applyCopperUpgrade(
            ItemUsageContext context,
            BlockState state,
            CopperUpgrade copperUpgrade
    ) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        ItemStack itemStack = context.getStack();
        BlockPos pos = context.getBlockPos();
        Block convertsTo = copperUpgrade.to();

        if (player != null) {
            itemStack.damage(1, player, hand.getEquipmentSlot());
        }

        BlockState updatedBlockState = copyProperties(
                convertsTo.getDefaultState(),
                state,
                copperUpgrade.copiedProperties()
        );

        boolean mergeInventories = copperUpgrade.mergeInventories() && !world.isClient();

        updateBlock(world, pos, player, updatedBlockState, mergeInventories);

        if (updatedBlockState.contains(Properties.CHEST_TYPE)) {
            ChestType chestType = updatedBlockState.get(Properties.CHEST_TYPE);

            if (!chestType.equals(ChestType.SINGLE)) {
                Direction facing = ChestBlock.getFacing(updatedBlockState);
                BlockPos oppositeSidePos = pos.offset(facing);
                BlockState oppositeSideState = world.getBlockState(oppositeSidePos);

                if (state.isOf(oppositeSideState.getBlock())) {
                    updateBlock(
                            world,
                            oppositeSidePos,
                            player,
                            updatedBlockState.withIfExists(Properties.CHEST_TYPE, chestType.getOpposite()),
                            mergeInventories
                    );
                }
            }
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            CWCriterion.USE_COPPER_UPGRADE_KIT.trigger(serverPlayer);
        }

        return ActionResult.SUCCESS;
    }

    protected static void updateBlock(
            World world,
            BlockPos pos,
            @Nullable PlayerEntity player,
            BlockState state,
            boolean mergeInventories
    ) {
        List<ItemStack> stacks = InventoryHelper.copy(world, pos, mergeInventories);

        world.setBlockState(pos, state);

        world.emitGameEvent(
                GameEvent.BLOCK_CHANGE,
                pos,
                GameEvent.Emitter.of(player, state)
        );

        if (mergeInventories) {
            List<ItemStack> overflow = InventoryHelper.addAll(world, pos, stacks);
            InventoryHelper.scatterItems(world, pos, overflow);
        }

        if (world.isClient()) {
            world.syncWorldEvent(
                    WorldEvents.BLOCK_BROKEN,
                    pos,
                    Block.getRawIdFromState(state)
            );
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static BlockState copyProperties(
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
