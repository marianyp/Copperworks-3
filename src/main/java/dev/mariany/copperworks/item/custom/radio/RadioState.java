package dev.mariany.copperworks.item.custom.radio;

public enum RadioState {
    AVAILABLE,
    UNAVAILABLE,
    BUSY;

    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    public boolean isBusy() {
        return this == BUSY;
    }
}
