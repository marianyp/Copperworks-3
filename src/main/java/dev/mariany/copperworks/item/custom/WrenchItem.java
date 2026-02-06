package dev.mariany.copperworks.item.custom;

import com.google.common.collect.ImmutableList;
import dev.mariany.copperworks.tag.CWTags;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.enums.RailShape;
import net.minecraft.block.enums.SlabType;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WrenchItem extends Item {
    private static final List<Property<?>> MODIFIABLE_PROPERTIES = List.of(
            Properties.ATTACHMENT,
            Properties.AXIS,
            Properties.BLOCK_FACE,
            Properties.BLOCK_HALF,
            Properties.FACING,
            Properties.HANGING,
            Properties.HOPPER_FACING,
            Properties.HORIZONTAL_FACING,
            Properties.RAIL_SHAPE,
            Properties.ROTATION,
            Properties.SLAB_TYPE,
            Properties.STRAIGHT_RAIL_SHAPE
    );

    private static final Map<Property<?>, Comparable<?>> BLACKLISTED_PROPERTY_VALUES = Map.of(
            Properties.SLAB_TYPE, SlabType.DOUBLE
    );

    private static final List<Direction> DIRECTION_ORDER = List.of(
            Direction.DOWN,
            Direction.UP,
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
    );

    public WrenchItem(Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent
                .builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                2.5,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                BASE_ATTACK_SPEED_MODIFIER_ID,
                                -3,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    public static boolean shouldOverrideInteraction(PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (player.getStackInHand(hand).getItem() instanceof WrenchItem) {
            World world = player.getEntityWorld();
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            return canWrench(world, pos, state) && getWrenchStates(world, pos, state).size() > 1;
        }

        return false;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (wrench(world, pos, state, player)) {
            stack.damage(1, player);

            SoundEvent soundEvent = state.isIn(CWTags.Blocks.LOUD) ? BlockSoundGroup.STONE.getPlaceSound() :
                    state.getSoundGroup().getPlaceSound();

            world.playSound(
                    player,
                    pos,
                    soundEvent,
                    SoundCategory.BLOCKS
            );

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    protected static boolean wrench(
            World world,
            BlockPos pos,
            BlockState state,
            @Nullable PlayerEntity player
    ) {
        if (!canWrench(world, pos, state)) {
            return false;
        }

        boolean backwards = player != null && player.isSneaking();

        if (MODIFIABLE_PROPERTIES.stream().noneMatch(state::contains)) {
            return false;
        }

        List<BlockState> states = getWrenchStates(world, pos, state);
        List<String> keys = states.stream().map(WrenchItem::getStateKey).toList();

        if (states.size() <= 1) {
            return false;
        }

        String currentKey = getStateKey(state);

        int index = keys.indexOf(currentKey);

        if (index < 0) {
            return false;
        }

        int size = states.size();
        int step = backwards ? -1 : 1;

        int nextIndex = Math.floorMod(index + step, size);
        BlockState nextState = states.get(nextIndex);

        world.setBlockState(pos, nextState);
        world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);

        return true;
    }

    protected static boolean canWrench(World world, BlockPos pos, BlockState state) {
        if (state.isIn(CWTags.Blocks.WRENCH_BLACKLIST)) {
            return false;
        }

        if (state.getHardness(world, pos) < 0) {
            return false;
        }

        if (state.get(Properties.EXTENDED, false)) {
            return false;
        }

        return state.get(Properties.CHEST_TYPE, ChestType.SINGLE).equals(ChestType.SINGLE);
    }

    protected static List<BlockState> getWrenchStates(World world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        StateManager<Block, BlockState> stateManager = block.getStateManager();

        ImmutableList<BlockState> states = stateManager.getStates();

        List<BlockState> filteredStates = new ArrayList<>();

        Map<Property<?>, Comparable<?>> properties = state.getEntries();

        Set<Map.Entry<Property<?>, Comparable<?>>> propertyEntries = state.getEntries().entrySet();

        Set<Map.Entry<Property<?>, Comparable<?>>> blackListedPropertyEntries = BLACKLISTED_PROPERTY_VALUES.entrySet();

        outer:
        for (BlockState possibleState : states) {
            for (Map.Entry<Property<?>, Comparable<?>> blacklistedPropertyEntry : blackListedPropertyEntries) {
                Property<?> property = blacklistedPropertyEntry.getKey();
                Comparable<?> value = blacklistedPropertyEntry.getValue();

                if (possibleState.contains(property) && possibleState.get(property).equals(value)) {
                    continue outer;
                }
            }

            for (Map.Entry<Property<?>, Comparable<?>> entry : propertyEntries) {
                Property<?> property = entry.getKey();
                Comparable<?> value = properties.get(property);

                if (!MODIFIABLE_PROPERTIES.contains(property)) {
                    if (!possibleState.get(property).equals(value)) {
                        continue outer;
                    }
                }
            }

            if (isValidState(world, pos, possibleState)) {
                filteredStates.add(possibleState);
            }
        }

        sortWrenchStates(filteredStates);

        return filteredStates;
    }

    protected static boolean isValidState(World world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof AbstractRailBlock railBlock) {
            RailShape railShape = state.get(railBlock.getShapeProperty());

            if (AbstractRailBlock.shouldDropRail(pos, world, railShape)) {
                return false;
            }
        }

        return state.canPlaceAt(world, pos);
    }

    protected static void sortWrenchStates(List<BlockState> states) {
        states.sort(Comparator.comparing(WrenchItem::getStateKey));
    }

    protected static String getStateKey(BlockState state) {
        return String.join("|", getStateKeys(state));
    }

    protected static List<String> getStateKeys(BlockState state) {
        return state.getEntries()
                    .entrySet()
                    .stream()
                    .map(WrenchItem::mapStateKey)
                    .toList();
    }

    protected static String mapStateKey(Map.Entry<Property<?>, Comparable<?>> entry) {
        return entry.getKey().toString() + ":" + normalizeComparable(entry.getValue());
    }

    private static String normalizeComparable(Comparable<?> value) {
        if (value instanceof Direction direction) {
            return normalizeDirection(direction);
        }

        if (value instanceof Number number) {
            return normalizeNumber(number);
        }

        return value.toString();
    }

    private static String normalizeDirection(Direction direction) {
        return normalizeNumber(DIRECTION_ORDER.indexOf(direction));
    }

    protected static String normalizeNumber(Number number) {
        double doubleValue = number.doubleValue();

        if (doubleValue >= 0 && doubleValue <= 9) {
            return "0" + number;
        }

        return number.toString();
    }
}
