package dev.mariany.copperworks.block.custom.muffler;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;

public record MuffledArea(BlockPos pos, int range) {
    public static final PacketCodec<RegistryByteBuf, MuffledArea> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, MuffledArea::pos,
            PacketCodecs.INTEGER, MuffledArea::range,
            MuffledArea::new
    );
}
