package dev.mariany.copperworks.block.custom.barrel;

import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.custom.InventoryNetworkBlockEntity;
import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class CopperBarrelBlockEntity extends InventoryNetworkBlockEntity {
    public CopperBarrelBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(CWBlockEntities.COPPER_BARREL, blockPos, blockState);
    }

    protected CopperBarrelBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(
                blockEntityType,
                pos,
                state,
                24,
                Text.translatable("container.copperworks.copper_barrel")
        );
    }

    @Override
    public void interact(PlayerEntity player, BlockPos pos) {
        super.interact(player, pos);
        playSound(player, pos);
    }

    protected static void playSound(PlayerEntity player, BlockPos pos) {
        World world = player.getEntityWorld();
        Random random = world.getRandom();

        world.playSound(
                player,
                pos,
                CWSoundEvents.BLOCK_COPPER_BARREL_OPEN,
                SoundCategory.BLOCKS,
                1,
                MathHelper.nextBetween(random, 0.9F, 1)
        );
    }
}
