package dev.mariany.copperworks.packet.serverbound;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.custom.muffler.MuffledArea;
import dev.mariany.copperworks.packet.clientbound.MuffledAreasPacket;
import dev.mariany.copperworks.properties.CWProperties;
import dev.mariany.copperworks.world.poi.CWPointOfInterestTypes;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;

import java.util.List;
import java.util.Objects;

public record RequestMufflersPacket(ChunkPos chunkPos) implements CustomPayload {
    public static final Id<RequestMufflersPacket> ID = new Id<>(
            Copperworks.id("request_mufflers")
    );

    public static final PacketCodec<ByteBuf, RequestMufflersPacket> CODEC = PacketCodec.tuple(
            ChunkPos.PACKET_CODEC, RequestMufflersPacket::chunkPos,
            RequestMufflersPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void apply(RequestMufflersPacket payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        ServerWorld world = player.getEntityWorld();
        ChunkPos chunkPos = payload.chunkPos();
        PointOfInterestStorage pointOfInterestStorage = world.getPointOfInterestStorage();

        List<BlockPos> mufflerPositions = pointOfInterestStorage
                .getInChunk(
                        poiType -> poiType.matchesKey(CWPointOfInterestTypes.MUFFLER),
                        chunkPos,
                        PointOfInterestStorage.OccupationStatus.ANY
                )
                .map(PointOfInterest::getPos)
                .toList();

        List<MuffledArea> muffledAreas = mufflerPositions.stream().map(pos -> {
            int muffleRange = world.getBlockState(pos).get(CWProperties.MUFFLE_RANGE, 0);

            if (muffleRange > 0) {
                return new MuffledArea(pos, muffleRange);
            }

            return null;
        }).filter(Objects::nonNull).toList();

        context.responseSender().sendPacket(new MuffledAreasPacket(chunkPos, muffledAreas));
    }
}
