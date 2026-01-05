package dev.mariany.copperworks.block.custom.relay.ender;

import dev.mariany.copperworks.block.CWBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class EnderRelayBlockEntity extends BlockEntity {
    protected ItemStack bindingStack = ItemStack.EMPTY;

    @Nullable
    protected LazyEntityReference<Entity> owner;

    protected long chunkTicketExpiryTicks;

    public EnderRelayBlockEntity(BlockPos pos, BlockState state) {
        this(CWBlockEntities.ENDER_RELAY, pos, state);
    }

    public EnderRelayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        if (this.world != null) {
            Block.dropStack(this.world, pos, this.bindingStack);
        }
    }

    public void setBindingStack(ItemStack bindingStack) {
        this.bindingStack = bindingStack;
    }

    public boolean isOwner(Entity entity) {
        return entity.equals(this.getOwner());
    }

    @Nullable
    public Entity getOwner() {
        if (this.world == null) {
            return null;
        }

        return this.owner != null && this.world instanceof ServerWorld serverWorld
                ? this.owner.getEntityByClass(serverWorld, Entity.class)
                : LazyEntityReference.getEntity(this.owner, this.world);
    }

    public void setOwner(@Nullable Entity owner) {
        this.setOwner(LazyEntityReference.of(owner));
    }

    public void setOwner(@Nullable LazyEntityReference<Entity> owner) {
        this.removeFromOwner();
        this.owner = owner;
        this.addToOwner();
    }

    protected void removeFromOwner() {
        if (this.getOwner() instanceof EnderRelayTracker enderRelayTracker) {
            this.getGlobalPos().ifPresent(enderRelayTracker::copperworks$removeEnderRelay);
        }
    }

    protected void addToOwner() {
        if (this.getOwner() instanceof EnderRelayTracker enderRelayTracker) {
            this.getGlobalPos().ifPresent(enderRelayTracker::copperworks$addEnderRelay);
        }
    }

    protected Optional<GlobalPos> getGlobalPos() {
        return Optional.ofNullable(
                this.world == null ? null : GlobalPos.create(
                        this.world.getRegistryKey(),
                        this.pos
                )
        );
    }

    public void tick(World world) {
        if (world instanceof ServerWorld serverWorld) {
            Entity entity = this.owner != null ? getPlayer(serverWorld, this.owner.getUuid()) : null;

            if (--this.chunkTicketExpiryTicks <= 0 && entity instanceof EnderRelayTracker enderRelayTracker) {
                this.chunkTicketExpiryTicks = this.getGlobalPos()
                                                  .map(enderRelayTracker::copperworks$handleEnderRelay)
                                                  .orElse(0L);
            }
        }
    }

    public void teleport() {
        Entity entity = this.getOwner();

        if (entity != null && this.world instanceof ServerWorld serverWorld) {
            if (!canTeleportEntityTo(entity, serverWorld)) {
                return;
            }

            Vec3d teleportPosition = this.pos.up().toBottomCenterPos();

            if (entity instanceof ServerPlayerEntity serverPlayer) {
                if (serverPlayer.networkHandler.isConnectionOpen()) {
                    entity.resetPortalCooldown();

                    ServerPlayerEntity teleportedPlayer = serverPlayer.teleportTo(
                            new TeleportTarget(
                                    serverWorld,
                                    teleportPosition,
                                    Vec3d.ZERO,
                                    0,
                                    0,
                                    PositionFlag.combine(PositionFlag.ROT, PositionFlag.DELTA),
                                    TeleportTarget.NO_OP
                            )
                    );

                    if (teleportedPlayer != null) {
                        teleportedPlayer.onLanding();
                        teleportedPlayer.clearCurrentExplosion();
                    }

                    playTeleportSound(serverWorld, teleportPosition);
                }
            } else {
                Entity teleportedEntity = entity.teleportTo(
                        new TeleportTarget(
                                serverWorld,
                                teleportPosition,
                                entity.getVelocity(),
                                entity.getYaw(),
                                entity.getPitch(),
                                TeleportTarget.NO_OP
                        )
                );

                if (teleportedEntity != null) {
                    teleportedEntity.onLanding();
                }

                playTeleportSound(serverWorld, teleportPosition);
            }
        }
    }

    private static boolean canTeleportEntityTo(Entity entity, World world) {
        if (entity.getEntityWorld().getRegistryKey() == world.getRegistryKey()) {
            return !(entity instanceof LivingEntity livingEntity) ? entity.isAlive() :
                    livingEntity.isAlive() && !livingEntity.isSleeping();
        }

        return entity.canUsePortals(true);
    }

    private static void playTeleportSound(World world, Vec3d pos) {
        world.playSound(
                null,
                pos.x,
                pos.y,
                pos.z,
                SoundEvents.ENTITY_PLAYER_TELEPORT,
                SoundCategory.PLAYERS,
                0.4F,
                1
        );
    }

    @Nullable
    private static Entity getPlayer(ServerWorld world, UUID uuid) {
        Entity entity = world.getEntityAnyDimension(uuid);
        return entity != null ? entity : world.getServer().getPlayerManager().getPlayer(uuid);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        EnderRelayData data = view.read(EnderRelayData.KEY, EnderRelayData.CODEC).orElse(EnderRelayData.DEFAULT);

        this.bindingStack = data.bindingStack();
        this.owner = data.owner().orElse(null);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.put(EnderRelayData.KEY, EnderRelayData.CODEC, new EnderRelayData(this.bindingStack, this.owner));
    }
}
