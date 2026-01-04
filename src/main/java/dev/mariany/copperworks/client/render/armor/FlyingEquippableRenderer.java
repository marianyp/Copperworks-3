package dev.mariany.copperworks.client.render.armor;

import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.component.FlyingEquippableComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class FlyingEquippableRenderer {
    public static void render(ItemStack stack, BipedEntityModel<?> model) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player != null) {
            render(player, model, stack);
        }
    }

    public static void render(
            LivingEntity livingEntity,
            BipedEntityModel<?> model,
            ItemStack stack
    ) {
        if (!FlyingEquippableComponent.shouldShowParticles(livingEntity)) {
            return;
        }

        FlyingEquippableComponent flyingEquippableComponent = stack.get(CWComponents.FLYING_EQUIPPABLE);

        if (flyingEquippableComponent == null) {
            return;
        }

        EquippableComponent equippableComponent = stack.get(DataComponentTypes.EQUIPPABLE);

        if (equippableComponent == null) {
            return;
        }

        render(livingEntity, model, flyingEquippableComponent.getParticleEffect(), equippableComponent.slot());
    }

    private static void render(
            LivingEntity livingEntity,
            BipedEntityModel<?> model,
            ParticleEffect particleEffect,
            EquipmentSlot slot
    ) {
        if (slot == EquipmentSlot.FEET) {
            renderFeet(livingEntity, model, particleEffect);
        }
    }

    private static void renderFeet(
            LivingEntity livingEntity,
            BipedEntityModel<?> model,
            ParticleEffect particleEffect
    ) {
        double pitchOffset = 0.05;
        double yOffsetFactor = -0.15;
        double zOffset = 0.1;

        double yOffset = (livingEntity.isGliding() ? 0.1 : 0) + yOffsetFactor;

        renderFoot(
                livingEntity,
                particleEffect,
                model.rightLeg.pitch + pitchOffset,
                yOffset,
                -zOffset
        );

        renderFoot(
                livingEntity,
                particleEffect,
                model.leftLeg.pitch + pitchOffset,
                yOffset,
                zOffset
        );
    }

    private static void renderFoot(
            LivingEntity livingEntity,
            ParticleEffect particleEffect,
            double pitch,
            double yOffset,
            double zOffset
    ) {
        World world = livingEntity.getEntityWorld();
        double bodyYaw = livingEntity.bodyYaw;
        double forwardOffsetX = Math.cos(bodyYaw * Math.PI / 180) * zOffset;
        double forwardOffsetZ = Math.sin(bodyYaw * Math.PI / 180) * zOffset;
        double sideOffsetX = Math.cos((bodyYaw - 90) * Math.PI / 180) * pitch;
        double sideOffsetZ = Math.sin((bodyYaw - 90) * Math.PI / 180) * pitch;

        world.addImportantParticleClient(
                particleEffect,
                true,
                livingEntity.getX() + forwardOffsetX + sideOffsetX,
                livingEntity.getY() + yOffset,
                livingEntity.getZ() + sideOffsetZ + forwardOffsetZ,
                0,
                0,
                0
        );
    }
}
