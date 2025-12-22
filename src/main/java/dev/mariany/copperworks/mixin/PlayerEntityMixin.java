package dev.mariany.copperworks.mixin;

import dev.mariany.copperworks.inventory.StorageNetwork;
import dev.mariany.copperworks.inventory.NetworkState;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements NetworkState {
    @Unique
    @Nullable
    private StorageNetwork network;

    @Override
    public Optional<StorageNetwork> copperworks2$getNetwork() {
        return Optional.ofNullable(this.network);
    }

    @Override
    public void copperworks2$setNetwork(@Nullable StorageNetwork network) {
        this.network = network;
    }
}
