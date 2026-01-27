package dev.mariany.copperworks.client.render.armor;

import dev.mariany.copperworks.client.CopperworksClient;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.component.FlyingEquippableComponent;
import dev.mariany.copperworks.component.FlyingEquippableStateComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class FlyingEquippableRenderer {
    private FlyingEquippableRenderer() {
    }

    public static void updateRenderState(LivingEntity livingEntity, LivingEntityRenderState state) {
        boolean dampenLimbAmplitude = CopperworksClient.getConfig().flyingEquippableDampensLimbAmplitude;

        if (dampenLimbAmplitude && FlyingEquippableStateComponent.hasVisibleParticles(livingEntity)) {
            float limbSwingAmplitude = state.limbSwingAmplitude;
            state.limbSwingAmplitude = limbSwingAmplitude > 0 ? limbSwingAmplitude / 4 : 0;
        }
    }

    public static void render(BipedEntityModel<?> model, BipedEntityRenderState state, ItemStack stack) {
        if (!CopperworksClient.getConfig().flyingEquippableParticles) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;

        if (world == null) {
            return;
        }

        FlyingEquippableComponent flyingEquippableComponent = stack.get(CWComponents.FLYING_EQUIPPABLE);
        FlyingEquippableStateComponent flyingEquippableStateComponent = stack.get(CWComponents.FLYING_EQUIPPABLE_STATE);

        if (flyingEquippableComponent == null || flyingEquippableStateComponent == null) {
            return;
        }

        if (!flyingEquippableStateComponent.areParticlesVisible()) {
            return;
        }

        EquippableComponent equippableComponent = stack.get(DataComponentTypes.EQUIPPABLE);

        if (equippableComponent == null) {
            return;
        }

        render(world, model, state, flyingEquippableComponent.particleEffect(), equippableComponent.slot());
    }

    private static void render(
            World world,
            BipedEntityModel<?> model,
            BipedEntityRenderState state,
            ParticleEffect particleEffect,
            EquipmentSlot slot
    ) {
        if (slot == EquipmentSlot.FEET) {
            renderFeet(world, model, state, particleEffect);
        }
    }

    private static void renderFeet(
            World world,
            BipedEntityModel<?> model,
            BipedEntityRenderState state,
            ParticleEffect particleEffect
    ) {
        double pitchOffset = 0.05;
        double yOffsetFactor = -0.15;
        double zOffset = 0.1;

        double yOffset = (state.isGliding ? 0.1 : 0) + yOffsetFactor;

        renderFoot(
                world,
                state,
                particleEffect,
                model.rightLeg.pitch + pitchOffset,
                yOffset,
                -zOffset
        );

        renderFoot(
                world,
                state,
                particleEffect,
                model.leftLeg.pitch + pitchOffset,
                yOffset,
                zOffset
        );
    }

    private static void renderFoot(
            World world,
            BipedEntityRenderState state,
            ParticleEffect particleEffect,
            double pitch,
            double yOffset,
            double zOffset
    ) {
        double bodyYaw = state.bodyYaw;
        double forwardOffsetX = Math.cos(bodyYaw * Math.PI / 180) * zOffset;
        double forwardOffsetZ = Math.sin(bodyYaw * Math.PI / 180) * zOffset;
        double sideOffsetX = Math.cos((bodyYaw - 90) * Math.PI / 180) * pitch;
        double sideOffsetZ = Math.sin((bodyYaw - 90) * Math.PI / 180) * pitch;

        world.addImportantParticleClient(
                particleEffect,
                true,
                state.x + forwardOffsetX + sideOffsetX,
                state.y + yOffset,
                state.z + sideOffsetZ + forwardOffsetZ,
                0,
                -0.025,
                0
        );
    }
}
