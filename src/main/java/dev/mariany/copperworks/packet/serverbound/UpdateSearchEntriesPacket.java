package dev.mariany.copperworks.packet.serverbound;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.screen.search.SearchableInventory;
import dev.mariany.copperworks.screen.search.SearchEntry;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public record UpdateSearchEntriesPacket(int syncId, List<SearchEntry> searchEntries) implements CustomPayload {
    public static final Id<UpdateSearchEntriesPacket> ID = new Id<>(
            Copperworks.id("update_search_entries")
    );

    public static final PacketCodec<ByteBuf, UpdateSearchEntriesPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, UpdateSearchEntriesPacket::syncId,
            SearchEntry.PACKET_CODEC.collect(PacketCodecs.toList()), UpdateSearchEntriesPacket::searchEntries,
            UpdateSearchEntriesPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void apply(UpdateSearchEntriesPacket payload, ServerPlayNetworking.Context context) {
        float syncId = payload.syncId();
        List<SearchEntry> entries = payload.searchEntries();

        ServerPlayerEntity player = context.player();
        ScreenHandler handler = player.currentScreenHandler;

        if (handler.syncId == syncId) {
            if (handler instanceof SearchableInventory searchableInventory) {
                searchableInventory.updateSearchEntries(entries);
            }
        }
    }
}
