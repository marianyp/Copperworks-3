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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

@Environment(EnvType.CLIENT)
public interface MufflerHandler {
    static void onDisconnect(ClientPlayNetworkHandler clientPlayNetworkHandler, MinecraftClient client) {
        Copperworks.LOGGER.info("Clearing Muffler Storage");
        MufflerStorage.clear();
    }

    static void onChunkLoad(ClientWorld world, WorldChunk worldChunk) {
        ClientPlayNetworking.send(new RequestMufflersPacket(worldChunk.getPos()));
    }

    static void onChunkUnload(ClientWorld world, WorldChunk worldChunk) {
        MufflerStorage.remove(worldChunk.getPos());
    }

    static boolean shouldMuffle(SoundInstance soundInstance) {
        Vec3d soundPos = new Vec3d(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());
        return shouldMuffle(soundPos);
    }

    static boolean shouldMuffle(Vec3d soundPos) {
        BlockPos pos = BlockPos.ofFloored(soundPos);
        return isMufflerNearby(pos, pos) || isMufflerNearPlayer(pos);
    }

    static boolean isMufflerNearPlayer(BlockPos soundPos) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player != null) {
            BlockPos playerPos = player.getBlockPos();
            return isMufflerNearby(playerPos, soundPos);
        }

        return false;
    }

    static boolean isMufflerNearby(BlockPos pos, BlockPos ignorePos) {
        for (MuffledArea muffledArea : MufflerStorage.getMuffledAreas()) {
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
