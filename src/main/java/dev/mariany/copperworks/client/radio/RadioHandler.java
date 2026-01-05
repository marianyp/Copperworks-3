package dev.mariany.copperworks.client.radio;

import dev.mariany.copperworks.block.custom.relay.radio.RadioRelayBlockEntity;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.item.custom.radio.RadioItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public interface RadioHandler {
    Map<Hand, BlockPos> RADIO_MAPPING = new HashMap<>();

    static void onTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;

        boolean newRadio = false;

        if (player != null) {
            World world = player.getEntityWorld();
            Map<Hand, BlockPos> previousState = Map.copyOf(RADIO_MAPPING);

            if (player.isSneaking()) {
                for (Hand hand : Hand.values()) {
                    if (refreshRadioState(player, hand)) {
                        newRadio = true;
                    }
                }
            } else {
                RADIO_MAPPING.clear();
            }

            for (Map.Entry<Hand, BlockPos> entry : previousState.entrySet()) {
                Hand hand = entry.getKey();
                BlockPos pos = entry.getValue();

                if (!RADIO_MAPPING.containsKey(hand) || !RADIO_MAPPING.get(hand).equals(pos)) {
                    if (world.getBlockEntity(pos) instanceof RadioRelayBlockEntity radioRelayBlockEntity) {
                        radioRelayBlockEntity.focus(false);
                    }
                }
            }

            if (newRadio) {
                for (BlockPos pos : RADIO_MAPPING.values()) {
                    if (world.getBlockEntity(pos) instanceof RadioRelayBlockEntity radioRelayBlockEntity) {
                        radioRelayBlockEntity.focus(true);
                    }
                }
            }
        }
    }

    private static boolean refreshRadioState(PlayerEntity player, Hand hand) {
        World world = player.getEntityWorld();
        ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem() instanceof RadioItem) {
            GlobalPos relayPosition = stack.get(CWComponents.RELAY_POSITION);

            if (relayPosition != null && world.getRegistryKey().equals(relayPosition.dimension())) {
                BlockPos pos = relayPosition.pos();

                if (!RADIO_MAPPING.containsKey(hand) || !RADIO_MAPPING.get(hand).equals(pos)) {
                    RADIO_MAPPING.put(hand, pos);
                    return true;
                }
            }
        } else {
            RADIO_MAPPING.remove(hand);
        }

        return false;
    }
}
