package dev.mariany.copperworks.client.muffler;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.block.custom.muffler.MuffledArea;
import dev.mariany.copperworks.packet.serverbound.RequestMufflersPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class MufflerHandler {
    private MufflerHandler() {}

    public static void onDisconnect(ClientPlayNetworkHandler clientPlayNetworkHandler, MinecraftClient client) {
        Copperworks.infoLog("Clearing Muffler Storage");
        MufflerStorage.clear();
    }

    public static void onChunkLoad(ClientWorld world, WorldChunk worldChunk) {
        ClientPlayNetworking.send(new RequestMufflersPacket(worldChunk.getPos()));
    }

    public static void onChunkUnload(ClientWorld world, WorldChunk worldChunk) {
        MufflerStorage.remove(worldChunk.getPos());
    }

    public static boolean shouldMuffle(SoundInstance soundInstance) {
        Vec3d soundPos = new Vec3d(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
        return shouldMuffle(soundPos);
    }

    public static boolean shouldMuffle(Vec3d soundPos) {
        BlockPos pos = BlockPos.ofFloored(soundPos);
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player != null) {
            if (isMufflerNearPlayer(player, pos)) {
                return true;
            }
        }

        return isMufflerNearby(pos, pos);
    }

    public static boolean isMufflerNearPlayer(PlayerEntity player, @Nullable BlockPos ignorePos) {
        if (player != null) {
            BlockPos playerPos = player.getBlockPos();
            return isMufflerNearby(playerPos, ignorePos);
        }

        return false;
    }

    private static boolean isMufflerNearby(BlockPos pos, @Nullable BlockPos ignorePos) {
        List<MuffledArea> muffledAreas = MufflerStorage.getMuffledAreas();

        for (MuffledArea muffledArea : muffledAreas) {
            int range = muffledArea.range();
            BlockPos mufflerPos = muffledArea.pos();

            if (mufflerPos.equals(ignorePos)) {
                continue;
            }

            if (mufflerPos.isWithinDistance(pos, range + 1)) {
                return true;
            }
        }

        return false;
    }
}
