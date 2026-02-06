package dev.mariany.copperworks.block.custom.relay;

import dev.mariany.copperworks.block.custom.relay.bound.BoundRelayBlock;
import dev.mariany.copperworks.block.custom.relay.ender.EnderRelayBlock;
import dev.mariany.copperworks.block.custom.relay.radio.RadioRelayBlock;
import dev.mariany.copperworks.item.CWItems;
import dev.mariany.copperworks.item.custom.radio.RadioItem;
import dev.mariany.copperworks.tag.CWTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
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
        boolean isRadio = stack.getItem() instanceof RadioItem;
        boolean isAmethystShard = stack.isOf(Items.AMETHYST_SHARD);
        boolean isAmethystPiece = stack.isOf(CWItems.AMETHYST_PIECE);
        boolean bindsEnderRelay = stack.isIn(CWTags.Items.BINDS_ENDER_RELAY);

        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (isAmethystShard) {
                BoundRelayBlock.initiateBinding(serverPlayer, stack, pos, hand);
            } else if (isAmethystPiece) {
                boolean completedBinding = BoundRelayBlock.completeBinding(
                        serverPlayer,
                        stack,
                        GlobalPos.create(world.getRegistryKey(), pos)
                );

                if (!completedBinding) {
                    return ActionResult.FAIL;
                }
            } else if (bindsEnderRelay) {
                EnderRelayBlock.completeBinding(serverPlayer.getEntityWorld(), pos, player, stack);
            } else if (isRadio) {
                RadioRelayBlock.completeBinding(world, pos, stack);
            } else {
                return ActionResult.FAIL;
            }

            return ActionResult.SUCCESS_SERVER;
        }

        if (isRadio || isAmethystShard || bindsEnderRelay) {
            return ActionResult.SUCCESS;
        }

        if (isAmethystPiece) {
            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }
}
