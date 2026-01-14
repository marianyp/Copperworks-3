package dev.mariany.copperworks.mixin.accessor;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerAccessor {
    @Accessor("syncHandler")
    ScreenHandlerSyncHandler copperworks$getSyncHandler();

    @Accessor("revision")
    void copperworks$setRevision(int revision);
}
