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
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public class FlyingEquippableStateComponent {
    public static final Codec<FlyingEquippableStateComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                                        Codec.BOOL.fieldOf("damage_queued")
                                                  .forGetter(FlyingEquippableStateComponent::isDamageQueued),
                                        Codec.DOUBLE.fieldOf("speed")
                                                    .forGetter(FlyingEquippableStateComponent::getSpeed),
                                        Codec.BOOL.fieldOf("prevented_fall_damage")
                                                  .forGetter(FlyingEquippableStateComponent::hasPreventedFallDamage),
                                        Codec.BOOL.fieldOf("particles_visible")
                                                  .forGetter(FlyingEquippableStateComponent::areParticlesVisible)
                                )
                                .apply(instance, FlyingEquippableStateComponent::new)
    );

    public static final PacketCodec<RegistryByteBuf, FlyingEquippableStateComponent> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN,
            FlyingEquippableStateComponent::isDamageQueued,
            PacketCodecs.DOUBLE,
            FlyingEquippableStateComponent::getSpeed,
            PacketCodecs.BOOLEAN,
            FlyingEquippableStateComponent::hasPreventedFallDamage,
            PacketCodecs.BOOLEAN,
            FlyingEquippableStateComponent::areParticlesVisible,
            FlyingEquippableStateComponent::new
    );

    private boolean damageQueued;
    private double speed;
    private boolean preventedFallDamage;
    private boolean particlesVisible;

    public FlyingEquippableStateComponent() {
        this(false, 0, false, false);
    }

    private FlyingEquippableStateComponent(
            boolean damageQueued,
            double speed,
            boolean preventedFallDamage,
            boolean particlesVisible
    ) {
        this.damageQueued = damageQueued;
        this.speed = speed;
        this.preventedFallDamage = preventedFallDamage;
        this.particlesVisible = particlesVisible;
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

    public boolean areParticlesVisible() {
        return this.particlesVisible;
    }

    public static boolean hasVisibleParticles(LivingEntity livingEntity) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack stack = livingEntity.getEquippedStack(equipmentSlot);
            FlyingEquippableStateComponent flyingEquippableComponent = stack.get(CWComponents.FLYING_EQUIPPABLE_STATE);

            if (flyingEquippableComponent != null && flyingEquippableComponent.particlesVisible) {
                return true;
            }
        }

        return false;
    }

    public static boolean canFly(LivingEntity livingEntity) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack stack = livingEntity.getEquippedStack(equipmentSlot);

            if (stack.contains(CWComponents.FLYING_EQUIPPABLE) && canFly(stack)) {
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
            @Nullable FlyingEquippableStateComponent newFlyingEquippableComponent
    ) {
        if (slot.isArmorSlot() && livingEntity instanceof PlayerEntity player) {
            if (newFlyingEquippableComponent == null) {
                resetAbilities(player);
            } else {
                updateAbilities(player);
            }
        }
    }

    public void inventoryTick(
            FlyingEquippableComponent flyingEquippableComponent,
            Entity entity,
            ItemStack stack,
            @Nullable EquipmentSlot slot
    ) {
        if (this.damageQueued && entity.age % flyingEquippableComponent.damageTickRate() == 0) {
            damageStack(entity, stack);
        }

        if (slot == null || !slot.isArmorSlot()) {
            return;
        }

        if (entity.isOnGround()) {
            this.preventedFallDamage = false;
        }

        if (entity instanceof LivingEntity livingEntity) {
            this.particlesVisible = shouldShowParticles(livingEntity);
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
                if (handleGlide(flyingEquippableComponent, livingEntity)) {
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

    private static boolean shouldShowParticles(LivingEntity livingEntity) {
        if (livingEntity.isSpectator()) {
            return false;
        }

        if (canFly(livingEntity)) {
            boolean flying = livingEntity instanceof PlayerEntity player && player.getAbilities().flying;
            boolean gliding = livingEntity.isGliding() && !isHalting(livingEntity);
            boolean savedFromFallDamage = wasSavedFromFallDamage(livingEntity);

            return flying || gliding || savedFromFallDamage;
        }

        return false;
    }

    private static boolean wasSavedFromFallDamage(LivingEntity livingEntity) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack stack = livingEntity.getEquippedStack(equipmentSlot);
            FlyingEquippableStateComponent flyingEquippableComponent = stack.get(CWComponents.FLYING_EQUIPPABLE_STATE);

            if (flyingEquippableComponent != null && flyingEquippableComponent.preventedFallDamage) {
                return true;
            }
        }

        return false;
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

    private boolean handleGlide(FlyingEquippableComponent flyingEquippableComponent, LivingEntity livingEntity) {
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
            double maximumSpeed = flyingEquippableComponent.maximumSpeed();
            double windUp = flyingEquippableComponent.windUp();

            Vec3d newVelocity = this.calculateNewVelocity(maximumSpeed, rotationVector, velocity);
            livingEntity.setVelocity(newVelocity);

            if ((angleChange / 1000) <= 0.025F) {
                this.increaseSpeed(livingEntity, maximumSpeed, windUp);
            } else {
                this.decreaseSpeed(livingEntity, maximumSpeed, windUp, angleChange);
            }
        } else {
            this.speed = 0;
        }

        return gliding;
    }

    private static boolean isHalting(Entity entitySneaking) {
        return !entitySneaking.isOnGround() && entitySneaking.isSneaking();
    }

    private Vec3d calculateNewVelocity(double maximumSpeed, Vec3d rotationVector, Vec3d velocity) {
        double scalingFactor = this.speed / maximumSpeed;
        double inertiaFactor = 1 - scalingFactor;

        return velocity.multiply(inertiaFactor).add(rotationVector.multiply(scalingFactor * maximumSpeed));
    }

    private void increaseSpeed(LivingEntity livingEntity, double maximumSpeed, double windUp) {
        double rate = windUp / 2;

        if (livingEntity.age % (rate * 20) == 0) {
            this.speed = MathHelper.clamp(
                    this.speed + windUp,
                    getMinimumSpeed(livingEntity, windUp),
                    maximumSpeed
            );
        }
    }

    private void decreaseSpeed(LivingEntity livingEntity, double maximumSpeed, double windUp, double angleChange) {
        double speedBaseline = (maximumSpeed / 2) + windUp;

        this.speed = MathHelper.clamp(
                maximumSpeed * (speedBaseline - angleChange),
                getMinimumSpeed(livingEntity, windUp),
                maximumSpeed
        );
    }

    private double getMinimumSpeed(LivingEntity livingEntity, double windUp) {
        return getGravity(livingEntity) + windUp;
    }

    private static double getGravity(LivingEntity livingEntity) {
        return livingEntity.getFinalGravity();
    }
}
