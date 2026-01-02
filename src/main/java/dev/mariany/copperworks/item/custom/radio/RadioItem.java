package dev.mariany.copperworks.item.custom.radio;

import dev.mariany.copperworks.block.CWBlocks;
import dev.mariany.copperworks.block.custom.relay.RadioBoundRelayBlock;
import dev.mariany.copperworks.block.custom.relay.RelayBlock;
import dev.mariany.copperworks.component.CWComponents;
import dev.mariany.copperworks.sound.CWSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
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

public class RadioItem extends Item {
    public RadioItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        ItemStack stack = context.getStack();
        Block block = world.getBlockState(pos).getBlock();

        if (block instanceof RelayBlock || block instanceof RadioBoundRelayBlock) {
            stack.set(CWComponents.RELAY_POSITION, new GlobalPos(world.getRegistryKey(), pos));
            world.setBlockState(pos, CWBlocks.RADIO_BOUND_RELAY.getDefaultState());
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if(player.isSneaking()) {
            player.getStackInHand(hand).remove(CWComponents.RELAY_POSITION);
            return ActionResult.SUCCESS;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            RadioState radioState = useRadio(serverPlayer, hand);

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

        return ActionResult.PASS;
    }

    protected static RadioState useRadio(ServerPlayerEntity player, Hand hand) {
        ServerWorld serverWorld = player.getEntityWorld();
        MinecraftServer server = serverWorld.getServer();
        ItemStack stack = player.getStackInHand(hand);

        GlobalPos globalPos = stack.get(CWComponents.RELAY_POSITION);
        RadioState radioState = RadioState.UNAVAILABLE;

        if (globalPos != null) {
            BlockPos otherPos = globalPos.pos();
            RegistryKey<World> otherDimension = globalPos.dimension();
            ServerWorld otherWorld = server.getWorld(otherDimension);

            if (otherWorld != null) {
                BlockState state = otherWorld.getBlockState(otherPos);

                if (state.getBlock() instanceof RadioBoundRelayBlock radioBoundRelayBlock) {
                    ChunkPos otherChunkPos = new ChunkPos(otherPos);

                    if (otherWorld.isTickingFutureReady(otherChunkPos.toLong())) {
                        if (state.get(RadioBoundRelayBlock.POWERED, false)) {
                            radioState = RadioState.BUSY;
                        } else {
                            radioState = RadioState.AVAILABLE;

                            otherWorld.setBlockState(
                                    otherPos,
                                    state.withIfExists(RadioBoundRelayBlock.POWERED, true)
                            );

                            otherWorld.scheduleBlockTick(
                                    otherPos,
                                    radioBoundRelayBlock,
                                    radioBoundRelayBlock.getPulseDuration()
                            );
                        }
                    }
                }
            }

        }

        return radioState;
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
