package dev.mariany.copperworks.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.jetbrains.annotations.Nullable;

public record FlyingEquippableComponent(
        ParticleEffect particleEffect,
        double maximumSpeed,
        double windUp,
        int damageTickRate
) {
    public static final Codec<FlyingEquippableComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                                        ParticleTypes.TYPE_CODEC.fieldOf("particle_effect")
                                                                .forGetter(FlyingEquippableComponent::particleEffect),
                                        Codec.DOUBLE.fieldOf("maximum_speed")
                                                    .forGetter(FlyingEquippableComponent::maximumSpeed),
                                        Codec.DOUBLE.fieldOf("wind_up")
                                                    .forGetter(FlyingEquippableComponent::windUp),
                                        Codec.INT.fieldOf("damage_tick_rate")
                                                 .forGetter(FlyingEquippableComponent::damageTickRate)
                                )
                                .apply(instance, FlyingEquippableComponent::new)
    );

    public static final PacketCodec<RegistryByteBuf, FlyingEquippableComponent> PACKET_CODEC = PacketCodec.tuple(
            ParticleTypes.PACKET_CODEC,
            FlyingEquippableComponent::particleEffect,
            PacketCodecs.DOUBLE,
            FlyingEquippableComponent::maximumSpeed,
            PacketCodecs.DOUBLE,
            FlyingEquippableComponent::windUp,
            PacketCodecs.INTEGER,
            FlyingEquippableComponent::damageTickRate,
            FlyingEquippableComponent::new
    );

    public static void onRemoveStack(
            LivingEntity livingEntity,
            EquipmentSlot slot,
            @Nullable FlyingEquippableStateComponent newFlyingEquippableStateComponent
    ) {
        ItemStack stack = livingEntity.getEquippedStack(slot);

        FlyingEquippableStateComponent flyingEquippableStateComponent = getOrCreateFlyingEquippableState(stack);
        flyingEquippableStateComponent.onRemoveStack(livingEntity, slot, newFlyingEquippableStateComponent);

        if (!stack.contains(CWComponents.FLYING_EQUIPPABLE_STATE)) {
            stack.set(CWComponents.FLYING_EQUIPPABLE_STATE, flyingEquippableStateComponent);
        }
    }

    public void inventoryTick(Entity entity, ItemStack stack, @Nullable EquipmentSlot slot) {
        FlyingEquippableStateComponent flyingEquippableStateComponent = getOrCreateFlyingEquippableState(stack);

        flyingEquippableStateComponent.inventoryTick(this, entity, stack, slot);

        if (!stack.contains(CWComponents.FLYING_EQUIPPABLE_STATE)) {
            stack.set(CWComponents.FLYING_EQUIPPABLE_STATE, flyingEquippableStateComponent);
        }
    }

    private static FlyingEquippableStateComponent getOrCreateFlyingEquippableState(ItemStack stack) {
        return stack.getOrDefault(CWComponents.FLYING_EQUIPPABLE_STATE, new FlyingEquippableStateComponent());
    }
}
