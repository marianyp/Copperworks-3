package dev.mariany.copperworks.block.custom.muffler;

import dev.mariany.copperworks.packet.clientbound.MuffledAreaUpdatedPacket;
import dev.mariany.copperworks.properties.CWProperties;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MufflerBlock extends Block {
    public static final IntProperty MUFFLE_RANGE = CWProperties.MUFFLE_RANGE;

    protected final int maxMuffleRange;

    public MufflerBlock(Settings settings) {
        super(settings);

        this.maxMuffleRange = MUFFLE_RANGE.stream()
                                          .mapToInt(Property.Value::value)
                                          .max()
                                          .orElse(0);

        this.setDefaultState(this.getDefaultState().with(MUFFLE_RANGE, this.maxMuffleRange));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MUFFLE_RANGE);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (world instanceof ServerWorld serverWorld) {
            if (oldState.getBlock() != state.getBlock()) {
                this.updateState(serverWorld, pos, state);
            }

            sendUpdatePackets(serverWorld, pos);
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        sendUpdatePackets(world, pos);
    }

    @Override
    protected void neighborUpdate(
            BlockState state,
            World world,
            BlockPos pos,
            Block sourceBlock,
            @Nullable WireOrientation wireOrientation,
            boolean notify
    ) {
        if (world instanceof ServerWorld serverWorld) {
            this.updateState(serverWorld, pos, state);
        }
    }

    protected void updateState(ServerWorld world, BlockPos pos, BlockState state) {
        int muffleRange = this.getMuffleRange(world, pos);

        if (muffleRange != state.get(MUFFLE_RANGE)) {
            BlockState newState = state.with(MUFFLE_RANGE, muffleRange);
            world.setBlockState(pos, newState);
            world.updateNeighborsAlways(pos, this, null);

            addToPoiStorage(world, pos, state);
        }
    }

    protected int getMuffleRange(World world, BlockPos pos) {
        return this.maxMuffleRange - world.getReceivedRedstonePower(pos);
    }

    protected static void addToPoiStorage(ServerWorld world, BlockPos pos, BlockState state) {
        PointOfInterestStorage poiStorage = world.getPointOfInterestStorage();

        Optional<RegistryEntry<PointOfInterestType>> optionalPoiType = poiStorage.getType(pos);

        if (optionalPoiType.isEmpty()) {
            PointOfInterestTypes.getTypeForState(state)
                                .ifPresent(poiType -> poiStorage.add(pos, poiType));
        }
    }

    protected static void sendUpdatePackets(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        MuffledArea muffledArea = new MuffledArea(pos, state.get(MUFFLE_RANGE, 0));

        world.getPlayers().forEach(player -> ServerPlayNetworking.send(
                player,
                new MuffledAreaUpdatedPacket(new ChunkPos(pos), muffledArea)
        ));
    }
}
