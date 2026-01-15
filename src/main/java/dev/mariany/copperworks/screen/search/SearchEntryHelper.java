package dev.mariany.copperworks.screen.search;

import dev.mariany.copperworks.Copperworks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SearchEntryHelper {
    private SearchEntryHelper() {
    }

    public static List<SearchEntry> getEntries(@Nullable World world, Collection<StackWithSlot> stacksWithSlot) {
        List<SearchEntry> searchEntries = new ArrayList<>();

        for (StackWithSlot stackWithSlot : stacksWithSlot) {
            searchEntries.add(new SearchEntry(world, stackWithSlot));
        }

        return searchEntries;
    }

    public static void batchSearchEntries(
            long maxBatchBytes,
            List<SearchEntry> searchEntries,
            BatchConsumer batchConsumer
    ) {
        if (searchEntries == null || searchEntries.isEmpty() || maxBatchBytes <= 0) {
            return;
        }

        final int entryCount = searchEntries.size();

        boolean sentAny = false;
        int index = 0;

        while (index < entryCount) {
            List<SearchEntry> batch = new ArrayList<>();
            long batchBytes = 0;

            while (index < entryCount) {
                SearchEntry entry = searchEntries.get(index);
                int entryBytes = SearchEntryHelper.measureEncodedBytes(entry);

                if (entryBytes > maxBatchBytes) {
                    if (batch.isEmpty()) {
                        boolean start = !sentAny;
                        boolean end = (index == entryCount - 1);

                        Copperworks.warnLog(
                                "Batching SearchEntry with size of {} bytes. This surpasses the {} byte limit",
                                entryBytes,
                                maxBatchBytes
                        );

                        batchConsumer.accept(List.of(entry), start, end);

                        sentAny = true;
                        index++;
                        batch = null;
                    }

                    break;
                }

                // If it doesn't fit in current batch, stop and send what we have.
                if (batchBytes + entryBytes > maxBatchBytes && !batch.isEmpty()) {
                    break;
                }

                batch.add(entry);

                batchBytes += entryBytes;
                index++;
            }

            if (batch == null) {
                continue;
            }

            if (batch.isEmpty()) {
                Copperworks.LOGGER.error("Unable to collect SearchEntry batch. At index ({}).", index);
                break;
            } else {
                boolean start = !sentAny;
                boolean end = (index == entryCount);

//                Copperworks.infoLog("Batching SearchEntry with size of {} bytes.", batchBytes);

                batchConsumer.accept(batch, start, end);

                sentAny = true;
            }
        }
    }

    public static int measureEncodedBytes(SearchEntry entry) {
        ByteBuf buf = Unpooled.buffer();

        try {
            SearchEntry.PACKET_CODEC.encode(buf, entry);
            return buf.readableBytes();
        } finally {
            buf.release();
        }
    }

    public interface BatchConsumer {
        void accept(List<SearchEntry> entries, boolean start, boolean end);
    }
}
