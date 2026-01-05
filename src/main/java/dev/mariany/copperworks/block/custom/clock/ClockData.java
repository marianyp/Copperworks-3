package dev.mariany.copperworks.block.custom.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mariany.copperworks.Copperworks;

public record ClockData(int target, int progress) {
    public static final String KEY = Copperworks.id("clock_data").toString();

    public static final ClockData DEFAULT = new ClockData(20, 0);

    public static final Codec<ClockData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                                        Codec.INT.fieldOf("target").forGetter(ClockData::target),
                                        Codec.INT.fieldOf("progress").forGetter(ClockData::progress)
                                )
                                .apply(instance, ClockData::new)
    );
}