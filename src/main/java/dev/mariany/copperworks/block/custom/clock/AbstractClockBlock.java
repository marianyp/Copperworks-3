package dev.mariany.copperworks.block.custom.clock;

import com.mojang.serialization.MapCodec;
import dev.mariany.copperworks.block.CWBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractClockBlock extends BlockWithEntity {
    public final static BooleanProperty POWERED = Properties.POWERED;

    private final int maxSeconds;
    private final int poweredTicks;

    public AbstractClockBlock(Settings settings, int maxSeconds, int poweredTicks) {
        super(settings);

        this.setDefaultState(
                this.stateManager.getDefaultState().with(POWERED, false)
        );

        this.maxSeconds = maxSeconds;
        this.poweredTicks = poweredTicks;
    }

    public int getMaxSeconds() {
        return this.maxSeconds;
    }

    public int getPoweredTicks() {
        return this.poweredTicks;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected abstract MapCodec<? extends AbstractClockBlock> getCodec();

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof ClockBlockEntity clockBlockEntity) {
            player.incrementStat(this.getInteractStat());
            clockBlockEntity.interact(player, pos);
        }

        return ActionResult.SUCCESS;
    }

    abstract protected Stat<Identifier> getInteractStat();

    @Override
    @Nullable
    public ClockBlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ClockBlockEntity(pos, state);
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.getWeakRedstonePower(world, pos, direction);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) ? 15 : 0;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return !world.isClient() ? validateTicker(
                type,
                CWBlockEntities.CLOCK,
                (tickWorld, pos, tickState, clockBlockEntity) ->
                        clockBlockEntity.tick(tickWorld, pos, tickState)
        ) : null;
    }
}
