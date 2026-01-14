package dev.mariany.copperworks.packet.serverbound;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.screen.search.SearchableInventory;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public record UpdateSearchQueryPacket(int syncId, @Nullable String searchQuery) implements CustomPayload {
    public static final Id<UpdateSearchQueryPacket> ID = new Id<>(
            Copperworks.id("update_search_query")
    );

    public static final PacketCodec<ByteBuf, UpdateSearchQueryPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, UpdateSearchQueryPacket::syncId,
            PacketCodecs.STRING, UpdateSearchQueryPacket::searchQuery,
            UpdateSearchQueryPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void apply(UpdateSearchQueryPacket payload, ServerPlayNetworking.Context context) {
        float syncId = payload.syncId();
        String query = payload.searchQuery();

        ServerPlayerEntity player = context.player();
        ScreenHandler handler = player.currentScreenHandler;

        player.updateLastActionTime();

        if (handler.syncId == syncId) {
            if (handler instanceof SearchableInventory searchableInventory) {
                searchableInventory.updateSearchQuery(query);
            }
        }
    }
}
