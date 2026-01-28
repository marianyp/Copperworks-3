package dev.mariany.copperworks.item.custom;

import dev.mariany.copperworks.item.CWItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;

public class PatinaItem extends Item {
    public PatinaItem(Settings settings) {
        super(settings);
    }

    public static void tryStrip(ServerWorld world, BlockPos pos, BlockState state) {
        if (world.getGameRules().getValue(GameRules.DO_TILE_DROPS)) {
            Oxidizable.getDecreasedOxidationState(state)
                      .ifPresent(strippedState -> Block.dropStack(
                              world,
                              pos,
                              CWItems.PATINA.getDefaultStack()
                      ));
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        return Oxidizable.getIncreasedOxidationBlock(state.getBlock())
                         .map(nextOxidationBlock -> {
                             world.setBlockState(pos, nextOxidationBlock.getStateWithProperties(state));
                             stack.decrementUnlessCreative(1, player);
                             world.playSound(
                                     null,
                                     pos,
                                     nextOxidationBlock.getDefaultState().getSoundGroup().getPlaceSound(),
                                     SoundCategory.BLOCKS
                             );
                             return (ActionResult) ActionResult.SUCCESS;
                         })
                         .orElse(ActionResult.PASS);
    }
}
