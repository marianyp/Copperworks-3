package dev.mariany.copperworks.block.custom.relay.bound;

import com.mojang.logging.LogUtils;
import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.block.custom.relay.HighlightedRelayBlockEntity;
import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

public class BoundRelayBlockEntity extends HighlightedRelayBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static final String BOUND_KEY = Copperworks.id("bound").toString();

    @Nullable
    protected GlobalPos bound = null;

    public BoundRelayBlockEntity(BlockPos pos, BlockState state) {
        this(CWBlockEntities.BOUND_RELAY, pos, state);
    }

    public BoundRelayBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state
    ) {
        super(type, pos, state, new Vec3d(0.8039, 0.6196, 0.9490));
    }

    public Optional<GlobalPos> getBoundPos() {
        return Optional.ofNullable(this.bound);
    }

    @Override
    public void focus(boolean focus) {
        this.animator.setFocused(focus, 4);
    }

    public void bind(GlobalPos pos) {
        this.bound = pos;
        this.markDirty();
    }

    protected static void disconnect(ServerWorld world, BlockPos pos) {
        MinecraftServer server = world.getServer();

        if (world.getBlockEntity(pos) instanceof BoundRelayBlockEntity boundRelayBlockEntity) {
            boundRelayBlockEntity.getBoundPos().ifPresent(boundPos -> {
                RegistryKey<World> dimension = boundPos.dimension();
                BlockPos otherPos = boundPos.pos();
                ServerWorld otherWorld = server.getWorld(dimension);

                if (otherWorld != null) {
                    BlockState state = otherWorld.getBlockState(otherPos);

                    if (state.getBlock() instanceof BoundRelayBlock) {
                        otherWorld.setBlockState(otherPos, CWBlocks.RELAY.getDefaultState());
                        playDisconnectSound(otherWorld, otherPos);
                    }
                }
            });
        }
    }

    protected static void playDisconnectSound(World world, BlockPos pos) {
        world.playSound(
                null,
                pos,
                CWSoundEvents.BLOCK_BOUND_RELAY_DISCONNECT,
                SoundCategory.BLOCKS,
                1,
                1
        );
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        if (this.world instanceof ServerWorld serverWorld) {
            disconnect(serverWorld, pos);
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbtCompound;

        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(this.getReporterContext(), LOGGER)) {
            NbtWriteView nbtWriteView = NbtWriteView.create(logging, registries);
            this.writeComponentlessData(nbtWriteView);
            nbtCompound = nbtWriteView.getNbt();
        }

        return nbtCompound;
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.bound = view.read(BOUND_KEY, GlobalPos.CODEC).orElse(null);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.put(BOUND_KEY, GlobalPos.CODEC, this.bound);
    }
}
