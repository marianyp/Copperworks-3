package dev.mariany.copperworks.item.custom;

import net.minecraft.block.Block;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.DoubleFunction;

public class AlternativeScaffoldingBlockItem extends BlockItem {
    public AlternativeScaffoldingBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    protected boolean canPlace(ItemPlacementContext context) {
        return this.canPlace(context, this.getBlock().getDefaultState());
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        boolean isClose = player.isSneaking();
        double blockInteractionRange = player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE);
        double startDistance = isClose ? 1.5 : blockInteractionRange;

        if (hand == Hand.OFF_HAND && player.getMainHandStack().isOf(this)) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            final double step = 0.5;

            DoubleFunction<ActionResult> tryPlaceAt = (distance) -> {
                BlockPos pos = getPlacePosition(player, distance);
                return this.place(createItemPlacementContext(player, hand, pos));
            };

            for (double distance = startDistance; distance > 0; distance -= step) {
                ActionResult result = tryPlaceAt.apply(distance);

                if (result.isAccepted()) {
                    return result;
                }
            }

            for (double distance = 0; distance <= blockInteractionRange; distance += step) {
                ActionResult result = tryPlaceAt.apply(distance);

                if (result.isAccepted()) {
                    return result;
                }
            }

            return ActionResult.FAIL;
        }

        return ActionResult.SUCCESS;
    }

    private static BlockPos getPlacePosition(PlayerEntity player, double distance) {
        Vec3d rotationVec = player.getRotationVec(1);

        double x = player.getX() + rotationVec.x * distance;
        double y = player.getEyeY() + rotationVec.y * distance;
        double z = player.getZ() + rotationVec.z * distance;

        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    private static ItemPlacementContext createItemPlacementContext(PlayerEntity player, Hand hand, BlockPos pos) {
        Direction side = Direction.getFacing(player.getRotationVec(1)).getOpposite();

        BlockHitResult blockHitResult = new BlockHitResult(
                Vec3d.ofBottomCenter(pos),
                side,
                pos,
                false
        );

        return new ItemPlacementContext(player, hand, player.getStackInHand(hand), blockHitResult);
    }
}
