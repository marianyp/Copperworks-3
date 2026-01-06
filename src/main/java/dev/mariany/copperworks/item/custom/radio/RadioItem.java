package dev.mariany.copperworks.item.custom.radio;

import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.block.custom.relay.radio.RadioRelayBlock;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RadioItem extends Item {
    public RadioItem(Settings settings) {
        super(settings);
    }

    public static void completeBinding(World world, BlockPos pos, ItemStack stack) {
        stack.set(CWComponents.RELAY_POSITION, new GlobalPos(world.getRegistryKey(), pos));
        world.setBlockState(pos, CWBlocks.RADIO_RELAY.getDefaultState());
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            RadioState radioState = useRadio(serverPlayer, hand);

            if (radioState != null) {
                if (!radioState.isBusy()) {
                    playSound(serverPlayer, radioState);

                    if (!radioState.isAvailable()) {
                        notifyUnavailable(serverPlayer);
                    }
                }

                if (radioState.isAvailable()) {
                    return ActionResult.SUCCESS_SERVER;
                }
            }
        }

        return ActionResult.PASS;
    }

    @Nullable
    protected static RadioState useRadio(ServerPlayerEntity player, Hand hand) {
        ServerWorld serverWorld = player.getEntityWorld();
        MinecraftServer server = serverWorld.getServer();
        ItemStack stack = player.getStackInHand(hand);

        GlobalPos globalPos = stack.get(CWComponents.RELAY_POSITION);

        if (globalPos == null) {
            return null;
        }

        BlockPos otherPos = globalPos.pos();
        RegistryKey<World> otherDimension = globalPos.dimension();
        ServerWorld otherWorld = server.getWorld(otherDimension);

        if (otherWorld != null) {
            ChunkPos otherChunkPos = new ChunkPos(otherPos);

            if (otherWorld.isTickingFutureReady(otherChunkPos.toLong())) {
                BlockState state = otherWorld.getBlockState(otherPos);

                if (state.getBlock() instanceof RadioRelayBlock radioRelayBlock) {
                    if (state.get(RadioRelayBlock.POWERED, false)) {
                        return RadioState.BUSY;
                    }

                    otherWorld.setBlockState(
                            otherPos,
                            state.withIfExists(RadioRelayBlock.POWERED, true)
                    );

                    otherWorld.scheduleBlockTick(
                            otherPos,
                            radioRelayBlock,
                            radioRelayBlock.getPulseDuration()
                    );

                    return RadioState.AVAILABLE;
                }
            }
        }

        return RadioState.UNAVAILABLE;
    }

    protected static void notifyUnavailable(PlayerEntity player) {
        player.sendMessage(Text.translatable("item.radio.unavailable"), true);
    }

    protected static void playSound(ServerPlayerEntity player, RadioState radioState) {
        player.networkHandler
                .sendPacket(
                        new PlaySoundS2CPacket(
                                CWSoundEvents.ITEM_RADIO,
                                SoundCategory.PLAYERS,
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                1,
                                radioState.isAvailable() ? 1 : 0.7F,
                                player.getRandom().nextLong()
                        )
                );
    }
}
