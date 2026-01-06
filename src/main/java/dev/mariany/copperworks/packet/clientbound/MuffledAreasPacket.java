package dev.mariany.copperworks.packet.clientbound;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.custom.muffler.MuffledArea;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.ChunkPos;

import java.util.List;

public record MuffledAreasPacket(ChunkPos chunkPos, List<MuffledArea> muffledAreas) implements CustomPayload {
    public static final Id<MuffledAreasPacket> ID = new Id<>(
            Copperworks.id("muffled_areas")
    );

    public static final PacketCodec<RegistryByteBuf, MuffledAreasPacket> CODEC = PacketCodec.tuple(
            ChunkPos.PACKET_CODEC, MuffledAreasPacket::chunkPos,
            MuffledArea.PACKET_CODEC.collect(PacketCodecs.toList()), MuffledAreasPacket::muffledAreas,
            MuffledAreasPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
