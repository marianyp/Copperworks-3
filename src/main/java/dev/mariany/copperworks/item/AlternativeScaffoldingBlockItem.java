package dev.mariany.copperworks.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        double distance = isClose ? 1.5 : player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE);

        if (hand.equals(Hand.OFF_HAND) && player.getMainHandStack().isOf(this)) {
            return ActionResult.PASS;
        }

        BlockPos pos = getPlacePositionWithRaycast(world, player, hand, distance);

        if (pos == null) {
            return ActionResult.FAIL;
        }

        if (!world.isClient()) {
            this.place(createItemPlacementContext(player, hand, pos));
        }

        return ActionResult.SUCCESS;
    }

    private static ItemPlacementContext createItemPlacementContext(PlayerEntity player, Hand hand, BlockPos pos) {
        BlockHitResult blockHitResult = new BlockHitResult(
                Vec3d.ofBottomCenter(pos),
                Direction.DOWN,
                pos,
                false
        );

        return new ItemPlacementContext(player, hand, player.getStackInHand(hand), blockHitResult);
    }

    @Nullable
    private BlockPos getPlacePositionWithRaycast(World world, PlayerEntity player, Hand hand, double maxDistance) {
        if (isEntityBlocking(world, player, maxDistance)) {
            return null;
        }

        double targetedFrameDistance = getTargetedDistance(world, player, maxDistance);
        double remainingDistance = targetedFrameDistance > -1 ? targetedFrameDistance : maxDistance;

        Vec3d startPos = player.getEyePos();
        Vec3d direction = player.getRotationVec(1.0F);

        while (remainingDistance > 0) {
            Vec3d endPos = startPos.add(direction.multiply(remainingDistance));

            RaycastContext context = new RaycastContext(
                    startPos,
                    endPos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player
            );

            BlockHitResult hitResult = world.raycast(context);

            BlockPos hitPos = hitResult.getBlockPos();
            BlockState hitBlockState = world.getBlockState(hitPos);
            Direction hitSide = hitResult.getSide();

            if (hitResult.getType() == HitResult.Type.MISS) {
                if (!hitBlockState.isOf(this.getBlock())) {
                    BlockPos placePos = BlockPos.ofFloored(endPos);

                    if (this.canPlace(createItemPlacementContext(player, hand, placePos))) {
                        return placePos;
                    }
                }
            }

            remainingDistance -= 0.5;

            if (remainingDistance <= 0) {
                return null;
            }

            if (!hitBlockState.isOf(this.getBlock())) {
                BlockPos placePos = hitPos.offset(hitSide);

                if (this.canPlace(createItemPlacementContext(player, hand, placePos))) {
                    return placePos;
                }
            }
        }

        return null;
    }

    private double getTargetedDistance(World world, PlayerEntity player, double maxDistance) {
        Double result = raycastIncrementally(
                world,
                player,
                maxDistance,
                (blockHitResult, distance) -> {
                    if (blockHitResult.getType() == HitResult.Type.MISS) {
                        BlockPos pos = blockHitResult.getBlockPos();
                        BlockState state = world.getBlockState(pos);

                        if (state.isOf(this.getBlock())) {
                            return distance;
                        }
                    }

                    return null;
                }
        );

        return result != null ? result : -1;
    }

    private static boolean isEntityBlocking(World world, PlayerEntity player, double maxDistance) {
        Boolean result = raycastIncrementally(
                world,
                player,
                maxDistance,
                (blockHitResult, distance) -> {
                    if (isEntityOccupyingSpace(world, blockHitResult.getBlockPos(), player)) {
                        return true;
                    }

                    return null;
                }
        );

        return result != null && result;
    }

    private static boolean isEntityOccupyingSpace(World world, @NotNull BlockPos pos, @Nullable PlayerEntity player) {
        return world.getNonSpectatingEntities(Entity.class, Box.from(Vec3d.of(pos)))
                    .stream()
                    .anyMatch(entity -> !entity.equals(player));
    }

    private static <T> T raycastIncrementally(
            World world,
            PlayerEntity player,
            double maxDistance,
            RayPredicate<T> rayPredicate
    ) {
        Vec3d startPos = player.getEyePos();
        Vec3d direction = player.getRotationVec(1);

        for (int distance = 0; distance < maxDistance; distance += 1) {
            Vec3d endPos = startPos.add(direction.multiply(distance));

            RaycastContext context = new RaycastContext(
                    startPos,
                    endPos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player
            );

            BlockHitResult blockHitResult = world.raycast(context);

            T out = rayPredicate.test(blockHitResult, distance);

            if (out != null) {
                return out;
            }
        }

        return null;
    }

    @FunctionalInterface
    private interface RayPredicate<T> {
        T test(BlockHitResult hit, double distance);
    }
}
