package dev.mariany.copperworks.item.upgrade.copper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.*;

public record CopperUpgrade(Block to, Set<String> copiedProperties, boolean mergeInventories) {
    public CopperUpgrade(Block to, Collection<String> copiedProperties, boolean mergeInventories) {
        this(to, new HashSet<>(copiedProperties), mergeInventories);
    }

    public static final Codec<CopperUpgrade> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                                        Registries.BLOCK
                                                .getEntryCodec()
                                                .xmap(
                                                        RegistryEntry::value,
                                                        Block::getRegistryEntry
                                                )
                                                .fieldOf("to").forGetter(CopperUpgrade::to),
                                        Codec.STRING
                                                .listOf()
                                                .fieldOf("copied_properties")
                                                .orElse(new ArrayList<>())
                                                .xmap(Set::copyOf, List::copyOf)
                                                .forGetter(CopperUpgrade::copiedProperties),
                                        Codec.BOOL
                                                .fieldOf("merge_inventories")
                                                .orElse(false)
                                                .forGetter(CopperUpgrade::mergeInventories)
                                )
                                .apply(instance, CopperUpgrade::new)
    );
}
