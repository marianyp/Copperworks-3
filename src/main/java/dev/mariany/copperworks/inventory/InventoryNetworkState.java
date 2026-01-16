package dev.mariany.copperworks.inventory;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface InventoryNetworkState {
    Optional<InventoryNetwork> copperworks$getNetwork();
    void copperworks$setNetwork(@Nullable InventoryNetwork network);
}
