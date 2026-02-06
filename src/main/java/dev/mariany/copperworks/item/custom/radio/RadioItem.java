package dev.mariany.copperworks.item.custom.radio;

import dev.mariany.copperworks.block.custom.relay.radio.RadioRelayBlock;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.sound.CWSoundEvents;
import dev.mariany.copperworks.sound.SoundHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class RadioItem extends Item {
    public RadioItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        GlobalPos globalPos = stack.get(CWComponents.RELAY_POSITION);

        if (globalPos == null) {
            return ActionResult.PASS;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            RelayStatus relayStatus = powerRelay(serverPlayer, globalPos);

            if (relayStatus != null) {
                if (!relayStatus.isBusy()) {
                    playSound(serverPlayer, relayStatus);
                }

                if (relayStatus.isAvailable()) {
                    return ActionResult.SUCCESS_SERVER;
                } else {
                    notifyUnavailable(serverPlayer);
                }
            }
        }

        return ActionResult.CONSUME;
    }

    protected static RelayStatus powerRelay(ServerPlayerEntity player, GlobalPos relayGlobalPos) {
        ServerWorld serverWorld = player.getEntityWorld();
        MinecraftServer server = serverWorld.getServer();

        BlockPos relayPos = relayGlobalPos.pos();
        RegistryKey<World> relayDimension = relayGlobalPos.dimension();
        ServerWorld relayWorld = server.getWorld(relayDimension);

        if (relayWorld != null) {
            if (relayWorld.isPosLoaded(relayPos)) {
                BlockState state = relayWorld.getBlockState(relayPos);

                if (state.getBlock() instanceof RadioRelayBlock radioRelayBlock) {
                    if (state.get(RadioRelayBlock.POWERED, false)) {
                        return RelayStatus.BUSY;
                    }

                    relayWorld.setBlockState(
                            relayPos,
                            state.withIfExists(RadioRelayBlock.POWERED, true)
                    );

                    relayWorld.scheduleBlockTick(
                            relayPos,
                            radioRelayBlock,
                            radioRelayBlock.getPulseDuration()
                    );

                    return RelayStatus.AVAILABLE;
                }
            }
        }

        return RelayStatus.UNAVAILABLE;
    }

    protected static void notifyUnavailable(PlayerEntity player) {
        player.sendMessage(Text.translatable("item.radio.unavailable"), true);
    }

    protected static void playSound(ServerPlayerEntity player, RelayStatus relayStatus) {
        SoundHelper.playSoundToPlayer(
                player,
                CWSoundEvents.ITEM_RADIO,
                SoundCategory.PLAYERS,
                1,
                relayStatus.isAvailable() ? 1 : 0.7F
        );
    }
}
