package dev.mariany.copperworks.client.muffler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class MuffledNotifier {
    private static final Text ENTER_TEXT = Text.translatable("block.copperworks.muffler.enter");
    private static final Text EXIT_TEXT = Text.translatable("block.copperworks.muffler.exit");

    private static boolean IN_MUFFLED_AREA;

    public static void onTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;

        if (player == null) {
            IN_MUFFLED_AREA = false;
            return;
        }

        boolean muffled = MufflerHandler.isMufflerNearPlayer(player, null);

        if (muffled != IN_MUFFLED_AREA) {
            Text text = muffled ? ENTER_TEXT : EXIT_TEXT;
            player.sendMessage(text, true);
            IN_MUFFLED_AREA = muffled;
        }
    }
}
