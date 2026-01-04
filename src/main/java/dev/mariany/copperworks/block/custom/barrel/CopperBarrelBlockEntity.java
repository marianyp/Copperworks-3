package dev.mariany.copperworks.block.custom.barrel;

import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.custom.InventoryNetworkBlockEntity;
import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ContainerUser;
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
    public void onOpen(ContainerUser user) {
        if (!this.isRemoved() && !user.asLivingEntity().isSpectator()) {
            playSound(user, this.pos, true);
        }
    }

    @Override
    public void onClose(ContainerUser user) {
        if (!this.isRemoved() && !user.asLivingEntity().isSpectator()) {
            playSound(user, this.pos, false);
        }
    }

    protected static void playSound(ContainerUser user, BlockPos pos, boolean opened) {
        playSound(user.asLivingEntity().getEntityWorld(), pos, opened);
    }

    protected static void playSound(World world, BlockPos pos, boolean open) {
        Random random = world.getRandom();

        float minPitch = open ? 0.9F : 0.6F;
        float maxPitch = open ? 1 : 0.7F;

        float volume = open ? 0.8F : 0.4F;

        world.playSound(
                null,
                pos,
                CWSoundEvents.BLOCK_COPPER_BARREL_LID_MOVED,
                SoundCategory.BLOCKS,
                volume,
                MathHelper.nextBetween(random, minPitch, maxPitch)
        );
    }
}
