package dev.mariany.copperworks.client.muffler;

import dev.mariany.copperworks.client.CopperworksClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class MufflerNotifier {
    private static final Text ENTER_TEXT = Text.translatable("block.copperworks.muffler.enter");
    private static final Text EXIT_TEXT = Text.translatable("block.copperworks.muffler.exit");

    private boolean inMuffledArea;

    public void onTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;

        if (player == null || !CopperworksClient.getConfig().enableMufflerNotifier) {
            this.inMuffledArea = false;
            return;
        }

        boolean muffled = MufflerHandler.isMufflerNearPlayer(player, null);

        if (muffled != this.inMuffledArea) {
            Text text = muffled ? ENTER_TEXT : EXIT_TEXT;
            player.sendMessage(text, true);
            this.inMuffledArea = muffled;
        }
    }
}
