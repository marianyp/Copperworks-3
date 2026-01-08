package dev.mariany.copperworks.client.muffler;

import dev.mariany.copperworks.block.custom.muffler.MuffledArea;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

@Environment(EnvType.CLIENT)
public interface MufflerStorage {
    Map<Long, Set<MuffledArea>> LOADED = new HashMap<>();

    static List<MuffledArea> getMuffledAreas() {
        return new ArrayList<>(LOADED.values())
                .stream()
                .flatMap(Collection::stream)
                .toList();
    }

    static void put(ChunkPos chunkPos, Collection<MuffledArea> muffledAreas) {
        LOADED.put(chunkPos.toLong(), new HashSet<>(muffledAreas));
    }

    static void remove(ChunkPos chunkPos) {
        LOADED.remove(chunkPos.toLong());
    }

    static void update(ChunkPos chunkPos, MuffledArea muffledArea) {
        Set<MuffledArea> muffledAreas = LOADED.get(chunkPos.toLong());

        if (muffledAreas == null) {
            return;
        }

        muffledAreas.removeIf(area -> area.pos().equals(muffledArea.pos()));

        if (muffledArea.range() > 0) {
            muffledAreas.add(muffledArea);
        }
    }

    static void clear() {
        LOADED.clear();
    }
}
