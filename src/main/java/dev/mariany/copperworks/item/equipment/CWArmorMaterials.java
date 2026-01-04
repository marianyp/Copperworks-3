package dev.mariany.copperworks.item.equipment;

import com.google.common.collect.Maps;
import dev.mariany.copperworks.sound.CWSoundEvents;
import dev.mariany.copperworks.tag.CWTags;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;

import java.util.Map;

public interface CWArmorMaterials {
    ArmorMaterial ROCKET_BOOTS = new ArmorMaterial(
            37,
            createDefenseMap(1, 0, 0, 0, 0),
            15,
            CWSoundEvents.ITEM_ARMOR_EQUIP_ROCKET_BOOTS,
            3F,
            0.1F,
            CWTags.Items.REPAIRS_ROCKET_BOOTS,
            CWEquipmentAssetKeys.ROCKET_BOOTS
    );

    private static Map<EquipmentType, Integer> createDefenseMap(
            int bootsDefense,
            int leggingsDefense,
            int chestplateDefense,
            int helmetDefense,
            int bodyDefense
    ) {
        return Maps.newEnumMap(
                Map.of(
                        EquipmentType.BOOTS,
                        bootsDefense,
                        EquipmentType.LEGGINGS,
                        leggingsDefense,
                        EquipmentType.CHESTPLATE,
                        chestplateDefense,
                        EquipmentType.HELMET,
                        helmetDefense,
                        EquipmentType.BODY,
                        bodyDefense
                )
        );
    }
}
