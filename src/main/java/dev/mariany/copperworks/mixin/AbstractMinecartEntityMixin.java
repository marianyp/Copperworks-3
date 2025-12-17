package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.event.entity.EntityEvents;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartEntity.class)
public class AbstractMinecartEntityMixin {
    @Inject(method = "move", at = @At(value = "HEAD"))
    protected void injectMove(MovementType type, Vec3d movement, CallbackInfo ci) {
        AbstractMinecartEntity abstractMinecart = (AbstractMinecartEntity) (Object) this;
        EntityEvents.BEFORE_MINECART_TRAVEL.invoker().onMinecartTravel(abstractMinecart);
    }
}
