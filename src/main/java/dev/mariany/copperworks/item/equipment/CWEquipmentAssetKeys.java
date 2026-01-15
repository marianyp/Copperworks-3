package dev.mariany.copperworks.item.equipment;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.RegistryKey;

public final class CWEquipmentAssetKeys {
    public static final RegistryKey<EquipmentAsset> ROCKET_BOOTS = register("rocket_boots");

    private CWEquipmentAssetKeys() {
    }

    private static RegistryKey<EquipmentAsset> register(String name) {
        return RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Copperworks.id(name));
    }
}
