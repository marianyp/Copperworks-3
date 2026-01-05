package dev.mariany.copperworks.block.custom.relay;

import dev.mariany.copperworks.advancement.criterion.CWCriterion;
import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.block.custom.relay.bound.BoundRelayBlockEntity;
import dev.mariany.copperworks.block.custom.relay.ender.EnderRelayBlock;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.item.CWItems;
import dev.mariany.copperworks.sound.CWSoundEvents;
import dev.mariany.copperworks.tag.CWTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class RelayBlock extends Block {
    public RelayBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUseWithItem(
            ItemStack stack,
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit
    ) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (stack.isOf(Items.AMETHYST_SHARD)) {
                ItemStack piece = ItemUsage.exchangeStack(
                        stack,
                        serverPlayer,
                        initiateBinding(world, pos),
                        false
                );

                serverPlayer.setStackInHand(hand, piece);

                playInsertSound(world, pos, false);
            } else if (stack.isOf(CWItems.AMETHYST_PIECE)) {
                if (!completeBinding(serverPlayer, stack, GlobalPos.create(world.getRegistryKey(), pos))) {
                    return ActionResult.FAIL;
                }

                stack.decrement(1);

                playInsertSound(world, pos, true);
            } else if (stack.isIn(CWTags.Items.BINDS_ENDER_RELAY)) {
                EnderRelayBlock.completeBinding(serverPlayer.getEntityWorld(), pos, player, stack);
            } else {
                return ActionResult.FAIL;
            }

            return ActionResult.SUCCESS_SERVER;
        }

        return ActionResult.CONSUME;
    }

    protected static ItemStack initiateBinding(World world, BlockPos pos) {
        ItemStack stack = CWItems.AMETHYST_PIECE.getDefaultStack();
        stack.set(CWComponents.RELAY_POSITION, GlobalPos.create(world.getRegistryKey(), pos));
        return stack;
    }

    protected static boolean completeBinding(ServerPlayerEntity player, ItemStack stack, GlobalPos globalPos) {
        ServerWorld world = player.getEntityWorld();
        MinecraftServer server = world.getServer();

        GlobalPos otherGlobalPos = stack.get(CWComponents.RELAY_POSITION);

        if (otherGlobalPos == null || otherGlobalPos.equals(globalPos)) {
            return false;
        }

        BlockPos thisPos = globalPos.pos();
        ServerWorld thisWorld = server.getWorld(globalPos.dimension());

        BlockPos otherPos = otherGlobalPos.pos();
        ServerWorld otherWorld = server.getWorld(otherGlobalPos.dimension());

        if (thisWorld == null || otherWorld == null) {
            return false;
        }

        BlockState state = otherWorld.getBlockState(otherPos);

        if (state.getBlock() instanceof RelayBlock) {
            createBoundRelay(thisWorld, thisPos, otherGlobalPos);
            createBoundRelay(otherWorld, otherPos, globalPos);
            CWCriterion.BOUND_RELAY.trigger(player);
            return true;
        }

        return false;
    }

    protected static void createBoundRelay(ServerWorld world, BlockPos pos, GlobalPos boundPos) {
        world.setBlockState(pos, CWBlocks.BOUND_RELAY.getDefaultState(), Block.NOTIFY_LISTENERS);

        if (world.getBlockEntity(pos) instanceof BoundRelayBlockEntity boundRelayBlockEntity) {
            boundRelayBlockEntity.bind(boundPos);
        }
    }

    protected static void playInsertSound(World world, BlockPos pos, boolean completed) {
        world.playSound(
                null,
                pos,
                CWSoundEvents.BLOCK_RELAY_INSERT,
                SoundCategory.BLOCKS,
                1,
                completed ? 1 : 1.6F
        );
    }
}
