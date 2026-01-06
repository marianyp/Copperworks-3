package dev.mariany.copperworks.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public class FlyingEquippableComponent {
    public static final Codec<FlyingEquippableComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                                        ParticleTypes.TYPE_CODEC.fieldOf("particle_effect")
                                                                .forGetter(FlyingEquippableComponent::getParticleEffect),
                                        Codec.DOUBLE.fieldOf("maximum_speed")
                                                    .forGetter(FlyingEquippableComponent::getMaximumSpeed),
                                        Codec.DOUBLE.fieldOf("wind_up")
                                                    .forGetter(FlyingEquippableComponent::getWindUp),
                                        Codec.INT.fieldOf("damage_tick_rate")
                                                 .forGetter(FlyingEquippableComponent::getDamageTickRate),
                                        Codec.BOOL.fieldOf("damage_queued")
                                                  .forGetter(FlyingEquippableComponent::isDamageQueued),
                                        Codec.DOUBLE.fieldOf("speed")
                                                    .forGetter(FlyingEquippableComponent::getSpeed),
                                        Codec.BOOL.fieldOf("prevented_fall_damage")
                                                  .forGetter(FlyingEquippableComponent::hasPreventedFallDamage)
                                )
                                .apply(instance, FlyingEquippableComponent::new)
    );

    public static final PacketCodec<RegistryByteBuf, FlyingEquippableComponent> PACKET_CODEC = PacketCodec.tuple(
            ParticleTypes.PACKET_CODEC,
            FlyingEquippableComponent::getParticleEffect,
            PacketCodecs.DOUBLE,
            FlyingEquippableComponent::getMaximumSpeed,
            PacketCodecs.DOUBLE,
            FlyingEquippableComponent::getWindUp,
            PacketCodecs.INTEGER,
            FlyingEquippableComponent::getDamageTickRate,
            PacketCodecs.BOOLEAN,
            FlyingEquippableComponent::isDamageQueued,
            PacketCodecs.DOUBLE,
            FlyingEquippableComponent::getSpeed,
            PacketCodecs.BOOLEAN,
            FlyingEquippableComponent::hasPreventedFallDamage,
            FlyingEquippableComponent::new
    );

    private final ParticleEffect particleEffect;
    private final double maximumSpeed;
    private final double windUp;
    private final int damageTickRate;
    private boolean damageQueued;
    private double speed;
    private boolean preventedFallDamage;

    public FlyingEquippableComponent(
            ParticleEffect particleEffect,
            double maximumSpeed,
            double windUp,
            int damageTickRate
    ) {
        this(
                particleEffect,
                maximumSpeed,
                windUp,
                damageTickRate,
                false,
                0,
                false
        );
    }

    private FlyingEquippableComponent(
            ParticleEffect particleEffect,
            double maximumSpeed,
            double windUp,
            int damageTickRate,
            boolean damageQueued,
            double speed,
            boolean preventedFallDamage
    ) {
        this.particleEffect = particleEffect;
        this.maximumSpeed = maximumSpeed;
        this.windUp = windUp;
        this.damageTickRate = damageTickRate;
        this.damageQueued = damageQueued;
        this.speed = speed;
        this.preventedFallDamage = preventedFallDamage;
    }

    public ParticleEffect getParticleEffect() {
        return this.particleEffect;
    }

    public double getMaximumSpeed() {
        return this.maximumSpeed;
    }

    public double getWindUp() {
        return this.windUp;
    }

    public int getDamageTickRate() {
        return this.damageTickRate;
    }

    public boolean isDamageQueued() {
        return this.damageQueued;
    }

    public double getSpeed() {
        return this.speed;
    }

    public boolean hasPreventedFallDamage() {
        return this.preventedFallDamage;
    }

    public static boolean shouldShowParticles(LivingEntity livingEntity) {
        boolean canFly = canFly(livingEntity);

        if (!canFly) {
            return false;
        }

        boolean flying = livingEntity instanceof PlayerEntity player && player.getAbilities().flying;
        boolean gliding = livingEntity.isGliding() && !isHalting(livingEntity);
        boolean savedFromFallDamage = wasSavedFromFallDamage(livingEntity);

        return flying || gliding || savedFromFallDamage;
    }

    public static boolean canFly(LivingEntity livingEntity) {
        if (livingEntity.isSpectator()) {
            return false;
        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack stack = livingEntity.getEquippedStack(equipmentSlot);

            if (stack.contains(CWComponents.FLYING_EQUIPPABLE) && canFly(stack)) {
                return true;
            }
        }

        return false;
    }

    public static boolean wasSavedFromFallDamage(LivingEntity livingEntity) {
        if (livingEntity.isSpectator()) {
            return false;
        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack stack = livingEntity.getEquippedStack(equipmentSlot);
            FlyingEquippableComponent flyingEquippableComponent = stack.get(CWComponents.FLYING_EQUIPPABLE);

            if (flyingEquippableComponent != null && flyingEquippableComponent.preventedFallDamage) {
                return true;
            }
        }

        return false;
    }

    private static boolean canFly(ItemStack stack) {
        return !stack.willBreakNextUse();
    }

    public void onRemoveStack(
            LivingEntity livingEntity,
            EquipmentSlot slot,
            @Nullable FlyingEquippableComponent newFlyingEquippableComponent
    ) {
        if (slot.isArmorSlot() && livingEntity instanceof PlayerEntity player) {
            if (newFlyingEquippableComponent == null) {
                resetAbilities(player);
            } else {
                updateAbilities(player);
            }
        }
    }

    public void inventoryTick(Entity entity, ItemStack stack, @Nullable EquipmentSlot slot) {
        if (this.damageQueued && entity.age % this.damageTickRate == 0) {
            damageStack(entity, stack);
        }

        if (slot == null || !slot.isArmorSlot()) {
            return;
        }

        if (entity.isOnGround()) {
            this.preventedFallDamage = false;
        }

        if (!canFly(stack) || entity.isSpectator()) {
            if (entity instanceof PlayerEntity player) {
                resetAbilities(player);
            }

            this.speed = 0;

            return;
        }

        if (entity instanceof PlayerEntity player) {
            updateAbilities(player);

            if (player.getAbilities().flying) {
                this.damageQueued = true;
            }
        }

        if (entity instanceof LivingEntity livingEntity) {
            if (canGlide(livingEntity)) {
                if (handleGlide(livingEntity)) {
                    this.damageQueued = true;
                }
            } else {
                this.speed = 0;
            }

            if (handleFallDamage(livingEntity)) {
                this.damageQueued = true;
                this.preventedFallDamage = true;
            }
        }
    }

    private static void updateAbilities(PlayerEntity player) {
        PlayerAbilities abilities = player.getAbilities();

        if (canGlide(player)) {
            resetAbilities(player);
        } else {
            abilities.allowFlying = true;
            abilities.setFlySpeed(0.02F);
        }
    }

    private static void resetAbilities(PlayerEntity player) {
        GameMode gameMode = player.getGameMode();

        if (gameMode != null) {
            PlayerAbilities abilities = player.getAbilities();
            abilities.setFlySpeed(0.05F);
            gameMode.setAbilities(abilities);
        }
    }

    private static boolean canGlide(LivingEntity livingEntity) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            if (LivingEntity.canGlideWith(livingEntity.getEquippedStack(equipmentSlot), equipmentSlot)) {
                return true;
            }
        }

        return false;
    }

    private static boolean handleFallDamage(LivingEntity livingEntity) {
        if (willTakeFallDamage(livingEntity)) {
            livingEntity.fallDistance = 1;
            return !isFallDamageImmune(livingEntity);
        }

        return false;
    }

    private static boolean willTakeFallDamage(LivingEntity livingEntity) {
        return livingEntity.fallDistance > livingEntity.getSafeFallDistance();
    }

    private static boolean isFallDamageImmune(LivingEntity livingEntity) {
        return livingEntity.isGliding() || livingEntity.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE) ||
                livingEntity.hasStatusEffect(StatusEffects.SLOW_FALLING);
    }

    private void damageStack(Entity entity, ItemStack stack) {
        if (!entity.getEntityWorld().isClient() && !stack.willBreakNextUse()) {
            stack.damage(1, entity instanceof PlayerEntity player ? player : null);
        }

        this.damageQueued = false;
    }

    private boolean handleGlide(LivingEntity livingEntity) {
        if (isHalting(livingEntity)) {
            this.speed = 0;
            return false;
        }

        boolean gliding = livingEntity.isGliding();

        if (gliding) {
            Vec3d rotationVector = livingEntity.getRotationVec(1F);
            Vec3d velocity = livingEntity.getVelocity();

            double cosAngle = velocity.normalize().dotProduct(rotationVector.normalize());
            double angleChange = Math.toDegrees(Math.acos(MathHelper.clamp(cosAngle, -1, 1)));

            Vec3d newVelocity = this.calculateNewVelocity(rotationVector, velocity);
            livingEntity.setVelocity(newVelocity);

            if ((angleChange / 1000) <= 0.025F) {
                this.increaseSpeed(livingEntity);
            } else {
                this.decreaseSpeed(livingEntity, angleChange);
            }
        } else {
            this.speed = 0;
        }

        return gliding;
    }

    private static boolean isHalting(Entity entitySneaking) {
        return !entitySneaking.isOnGround() && entitySneaking.isSneaking();
    }

    private Vec3d calculateNewVelocity(Vec3d rotationVector, Vec3d velocity) {
        double scalingFactor = this.speed / this.maximumSpeed;
        double inertiaFactor = 1 - scalingFactor;

        return velocity.multiply(inertiaFactor).add(rotationVector.multiply(scalingFactor * this.maximumSpeed));
    }

    private void increaseSpeed(LivingEntity livingEntity) {
        double rate = this.windUp / 2;

        if (livingEntity.age % (rate * 20) == 0) {
            this.speed = MathHelper.clamp(
                    this.speed + this.windUp,
                    getMinimumSpeed(livingEntity),
                    this.maximumSpeed
            );
        }
    }

    private void decreaseSpeed(LivingEntity livingEntity, double angleChange) {
        double speedBaseline = (this.maximumSpeed / 2) + this.windUp;

        this.speed = MathHelper.clamp(
                this.maximumSpeed * (speedBaseline - angleChange),
                getMinimumSpeed(livingEntity),
                this.maximumSpeed
        );
    }

    private double getMinimumSpeed(LivingEntity livingEntity) {
        return getGravity(livingEntity) + this.windUp;
    }

    private static double getGravity(LivingEntity livingEntity) {
        return livingEntity.getFinalGravity();
    }
}
