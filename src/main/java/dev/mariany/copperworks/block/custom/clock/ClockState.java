package dev.mariany.copperworks.block.custom.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mariany.copperworks.Copperworks;

public record ClockState(int target, int progress) {
    public static final String KEY = Copperworks.id("clock_state").toString();

    public static final ClockState DEFAULT = new ClockState(ClockBlockEntity.SECOND_IN_TICKS, 0);

    public static final Codec<ClockState> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                                        Codec.INT.fieldOf("target").forGetter(ClockState::target),
                                        Codec.INT.fieldOf("progress").forGetter(ClockState::progress)
                                )
                                .apply(instance, ClockState::new)
    );
}