package dev.mariany.copperworks.packet.clientbound;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.custom.muffler.MuffledArea;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.ChunkPos;

public record MuffledAreaUpdatedPacket(ChunkPos chunkPos, MuffledArea muffledArea) implements CustomPayload {
    public static final Id<MuffledAreaUpdatedPacket> ID = new Id<>(
            Copperworks.id("muffled_area")
    );

    public static final PacketCodec<RegistryByteBuf, MuffledAreaUpdatedPacket> CODEC = PacketCodec.tuple(
            ChunkPos.PACKET_CODEC, MuffledAreaUpdatedPacket::chunkPos,
            MuffledArea.PACKET_CODEC, MuffledAreaUpdatedPacket::muffledArea,
            MuffledAreaUpdatedPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
