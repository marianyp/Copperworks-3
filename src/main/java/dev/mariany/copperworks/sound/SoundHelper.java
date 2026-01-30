package dev.mariany.copperworks.sound;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class SoundHelper {
    private SoundHelper() {
    }

    public static void playSoundToPlayer(
            PlayerEntity player,
            SoundEvent sound,
            SoundCategory category,
            float volume,
            float pitch
    ) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.networkHandler.sendPacket(
                    new PlaySoundS2CPacket(
                            Registries.SOUND_EVENT.getEntry(sound),
                            category,
                            x,
                            y,
                            z,
                            volume,
                            pitch,
                            serverPlayer.getRandom().nextLong()
                    )
            );
        } else {
            player.getEntityWorld().playSoundClient(
                    x,
                    y,
                    z,
                    sound,
                    category,
                    volume,
                    pitch,
                    false
            );
        }
    }
}
