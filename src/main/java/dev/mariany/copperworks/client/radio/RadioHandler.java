package dev.mariany.copperworks.client.radio;

import dev.mariany.copperworks.block.custom.relay.RadioBoundRelayBlockEntity;
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
public class RadioHandler {
    private final Map<Hand, BlockPos> radioMapping = new HashMap<>();

    public void onTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;

        if (player != null) {
            World world = player.getEntityWorld();
            Map<Hand, BlockPos> previousState = Map.copyOf(this.radioMapping);

            if (player.isSneaking()) {
                boolean newRadio = false;

                for (Hand hand : Hand.values()) {
                    if (refreshRadioState(player, hand)) {
                        newRadio = true;
                    }
                }

                if (newRadio) {
                    for (BlockPos pos : this.radioMapping.values()) {
                        if (world.getBlockEntity(pos) instanceof RadioBoundRelayBlockEntity radioBoundRelayBlock) {
                            radioBoundRelayBlock.focus(true);
                        }
                    }
                }
            } else {
                this.radioMapping.clear();
            }

            for (Map.Entry<Hand, BlockPos> entry : previousState.entrySet()) {
                Hand hand = entry.getKey();
                BlockPos pos = entry.getValue();

                if (!this.radioMapping.containsKey(hand) || !this.radioMapping.get(hand).equals(pos)) {
                    if (world.getBlockEntity(pos) instanceof RadioBoundRelayBlockEntity radioBoundRelayBlock) {
                        radioBoundRelayBlock.focus(false);
                    }
                }
            }
        }
    }

    private boolean refreshRadioState(PlayerEntity player, Hand hand) {
        World world = player.getEntityWorld();
        ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem() instanceof RadioItem) {
            GlobalPos relayPosition = stack.get(CWComponents.RELAY_POSITION);

            if (relayPosition != null && world.getRegistryKey().equals(relayPosition.dimension())) {
                BlockPos pos = relayPosition.pos();

                if (!this.radioMapping.containsKey(hand) || !this.radioMapping.get(hand).equals(pos)) {
                    this.radioMapping.put(hand, pos);
                    return true;
                }
            }
        } else {
            this.radioMapping.remove(hand);
        }

        return false;
    }
}
