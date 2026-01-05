package dev.mariany.copperworks.component;

import com.mojang.serialization.Codec;
import dev.mariany.copperworks.Copperworks;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.GlobalPos;

import java.util.function.UnaryOperator;

public class CWComponents {
    public static final ComponentType<GlobalPos> RELAY_POSITION = register(
            "relay/position",
            builder -> builder.codec(GlobalPos.CODEC).packetCodec(GlobalPos.PACKET_CODEC)
    );

    public static final ComponentType<Integer> DRAGON_BREATH_FULLNESS = register(
            "dragon_breath/fullness",
            builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.INTEGER)
    );

    public static final ComponentType<FlyingEquippableComponent> FLYING_EQUIPPABLE = register(
            "flying_equippable",
            builder -> builder
                    .codec(FlyingEquippableComponent.CODEC)
                    .packetCodec(FlyingEquippableComponent.PACKET_CODEC)
    );

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                Copperworks.id(id),
                builderOperator.apply(ComponentType.builder()).build()
        );
    }

    public static void bootstrap() {
        Copperworks.bootstrapLog("Components");
    }
}
