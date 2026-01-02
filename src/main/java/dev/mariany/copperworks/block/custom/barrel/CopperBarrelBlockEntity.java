package dev.mariany.copperworks.block.custom.barrel;

import dev.mariany.copperworks.block.CWBlockEntities;
import dev.mariany.copperworks.inventory.InventoryNetwork;
import dev.mariany.copperworks.inventory.InventoryNetworkContainer;
import dev.mariany.copperworks.inventory.InventoryNetworkState;
import dev.mariany.copperworks.packet.clientbound.InventoryNetworkUpdatePacket;
import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import dev.mariany.copperworks.stat.CWStats;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class CopperBarrelBlockEntity extends BlockEntity
        implements InventoryNetworkContainer, NamedScreenHandlerFactory, Nameable {
    protected InventoryNetwork network;
    protected final int slotsPerConnection;

    public CopperBarrelBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(CWBlockEntities.COPPER_BARREL, blockPos, blockState);
    }

    protected CopperBarrelBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        this(blockEntityType, pos, blockState, 24);
    }

    protected CopperBarrelBlockEntity(
            BlockEntityType<?> blockEntityType,
            BlockPos pos,
            BlockState blockState,
            int slotsPerConnection
    ) {
        super(blockEntityType, pos, blockState);
        this.slotsPerConnection = slotsPerConnection;
        this.network = this.createNetwork();
    }

    public static void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        InventoryNetworkContainer.tick(blockEntity);
    }

    public static void interact(CopperBarrelBlockEntity copperBarrelBlockEntity, PlayerEntity player, BlockPos pos) {
        player.incrementStat(CWStats.OPEN_COPPER_BARREL);

        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            GlobalPos globalPos = GlobalPos.create(serverWorld.getRegistryKey(), pos);
            InventoryNetwork network = copperBarrelBlockEntity.getNetwork();

            if (player instanceof InventoryNetworkState networkState) {
                networkState.copperworks2$setNetwork(copperBarrelBlockEntity.getNetwork());
            }

            if (player instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new InventoryNetworkUpdatePacket(globalPos, network));
            }

            PiglinBrain.onGuardedBlockInteracted(serverWorld, player, true);
        }
    }

    public void onBlockAdded(World world, BlockPos pos, CopperBarrelBlockEntity copperBarrelBlockEntity) {
        Set<BlockPos> discoveredControllers = new HashSet<>();

        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos.offset(direction);
            BlockEntity neighboringBlockEntity = world.getBlockEntity(offsetPos);

            if (neighboringBlockEntity instanceof CopperBarrelBlockEntity otherCopperBarrelBlockEntity) {
                InventoryNetwork network = otherCopperBarrelBlockEntity.getNetwork();

                network.getControllerPos().ifPresent(controllerPos -> {
                    copperBarrelBlockEntity.connect(
                            world,
                            otherCopperBarrelBlockEntity,
                            !discoveredControllers.contains(controllerPos)
                    );

                    discoveredControllers.add(controllerPos);
                });
            }
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        InventoryNetworkContainer.onRemoved(this, this.pos);
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        // Prevents default item scatter behavior
    }

    @Override
    @Nullable
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new InventoryNetworkScreenHandler(syncId, playerInventory);
    }

    @Override
    public Text getDisplayName() {
        return this.getName();
    }

    @Override
    public Text getName() {
        return this.network.getCustomName().orElse(Text.translatable("container.copperworks.copper_barrel"));
    }

    @Override
    public InventoryNetwork createNetwork() {
        return new InventoryNetwork(this.world, pos, this.slotsPerConnection);
    }

    @Override
    public InventoryNetwork getNetwork() {
        return this.network;
    }

    @Override
    public void setNetwork(InventoryNetwork network) {
        this.network = network;
        this.markDirty();
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.network.setWorld(world);
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        InventoryNetworkContainer.readComponents(components, this);
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        InventoryNetworkContainer.addComponents(builder, this);
    }

    @Override
    public void removeFromCopiedStackData(WriteView view) {
        InventoryNetworkContainer.removeFromCopiedStackData(view);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        InventoryNetworkContainer.readData(view, this, this.world, this.pos);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        InventoryNetworkContainer.writeData(view, this, this.pos);
    }
}
