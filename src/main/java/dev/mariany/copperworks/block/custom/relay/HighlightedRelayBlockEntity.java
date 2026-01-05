package dev.mariany.copperworks.block.custom.relay;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class HighlightedRelayBlockEntity extends BlockEntity {
    protected final Animator animator = new Animator();
    protected final Vec3d color;

    public HighlightedRelayBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            Vec3d color
    ) {
        super(type, pos, state);
        this.color = color;
    }

    public static void clientTick(
            World world,
            BlockPos pos,
            BlockState state,
            HighlightedRelayBlockEntity blockEntity
    ) {
        blockEntity.animator.step();
    }

    public abstract void focus(boolean focus);

    public Vec3d getColor() {
        return this.color;
    }

    public float getAnimationProgress(float tickProgress) {
        return this.animator.getProgress(tickProgress);
    }

    public static class Animator {
        private static final int INFINITE_ITERATIONS = -1;
        private boolean focused;
        private float progress;
        private float lastProgress;
        private boolean forward = true;
        private int iterations = INFINITE_ITERATIONS;

        public float getProgress(float tickProgress) {
            return MathHelper.lerp(tickProgress, this.lastProgress, this.progress);
        }

        public void setFocused(boolean focused) {
            setFocused(focused, INFINITE_ITERATIONS);
        }

        public void setFocused(boolean focused, int iterations) {
            this.focused = focused;
            this.iterations = iterations;

            if (!focused) {
                this.progress = 0;
                this.lastProgress = 0;
                this.forward = true;
            }
        }

        public void reset() {
            this.progress = 0;
            this.forward = true;
        }

        public void step() {
            step(0.1F);
        }

        public void step(float step) {
            this.lastProgress = this.progress;

            if (!this.focused || this.iterations == 0) {
                reset();
                return;
            }

            if (forward) {
                this.progress += step;

                if (this.progress >= 1) {
                    this.progress = 1;
                    this.forward = false;
                }
            } else {
                this.progress -= step;

                if (this.progress <= 0) {
                    reset();

                    if (this.iterations > 0) {
                        this.iterations -= 1;
                    }
                }
            }
        }
    }
}
