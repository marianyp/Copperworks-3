package dev.mariany.copperworks.server.world;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ChunkTicketType;

public interface CWChunkTickets {
    ChunkTicketType ENDER_RELAY = register(
            "ender_relay",
            40,
            ChunkTicketType.FOR_LOADING | ChunkTicketType.FOR_SIMULATION | ChunkTicketType.RESETS_IDLE_TIMEOUT
    );

    private static ChunkTicketType register(String id, long expiryTicks, int flags) {
        return Registry.register(Registries.TICKET_TYPE, Copperworks.id(id), new ChunkTicketType(expiryTicks, flags));
    }

    static void bootstrap() {
        Copperworks.bootstrapLog("Chunk Tickets");
    }
}
