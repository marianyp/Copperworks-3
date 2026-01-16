package dev.mariany.copperworks.mixin.accessor;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("doubleClicking")
    boolean copperworks$isDoubleClicking();

    @Accessor("doubleClicking")
    void copperworks$setDoubleClicking(boolean doubleClicking);

    @Accessor("quickMovingStack")
    ItemStack copperworks$getQuickMovingStack();

    @Invoker("getSlotAt")
    @Nullable
    Slot copperworks$getSlotAt(double mouseX, double mouseY);
}
