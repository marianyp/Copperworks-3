package dev.mariany.copperworks.inventory;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface NetworkState {
    Optional<StorageNetwork> copperworks2$getNetwork();
    void copperworks2$setNetwork(@Nullable StorageNetwork network);
}
