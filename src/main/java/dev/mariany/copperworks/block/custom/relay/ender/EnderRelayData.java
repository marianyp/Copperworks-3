package dev.mariany.copperworks.block.custom.relay.ender;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mariany.copperworks.Copperworks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record EnderRelayData(ItemStack bindingStack, Optional<LazyEntityReference<Entity>> owner) {
    public static final String KEY = Copperworks.id("ender_relay_data").toString();

    public static final EnderRelayData DEFAULT = new EnderRelayData(ItemStack.EMPTY, Optional.empty());

    public static final Codec<EnderRelayData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                                        ItemStack.MAP_CODEC.fieldOf("binding_stack")
                                                           .forGetter(EnderRelayData::bindingStack),
                                        LazyEntityReference.<Entity>createCodec()
                                                           .optionalFieldOf("owner")
                                                           .forGetter(EnderRelayData::owner)
                                )
                                .apply(instance, EnderRelayData::new)
    );

    public EnderRelayData(ItemStack bindingStack, @Nullable LazyEntityReference<Entity> owner) {
        this(bindingStack, Optional.ofNullable(owner));
    }
}
