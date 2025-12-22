package dev.mariany.copperworks.block.entity;

import com.mojang.logging.LogUtils;
import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.inventory.InventoryNetwork;
import dev.mariany.copperworks.screen.CopperBarrelScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

public class CopperBarrelBlockEntity extends BlockEntity implements Inventory, NamedScreenHandlerFactory, Nameable {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String NETWORK_NAME_KEY = Copperworks.id("network").toString();

    protected InventoryNetwork network;

    public CopperBarrelBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(CWBlockEntities.COPPER_BARREL, blockPos, blockState);
    }

    protected CopperBarrelBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.network = new InventoryNetwork(this.world, pos, 24);
    }

    public InventoryNetwork getNetwork() {
        return this.network;
    }

    public void setNetwork(InventoryNetwork network) {
        this.network = network;
        this.markDirty();
    }

    public boolean isController() {
        return this.network.getControllerPos()
                           .map(controllerPos -> controllerPos.equals(this.pos))
                           .orElse(false);
    }

    public void connect(World world, CopperBarrelBlockEntity otherCopperBarrelBlockEntity, boolean mergeItems) {
        List<BlockPos> connectionPositions = this.network.getConnections();

        otherCopperBarrelBlockEntity.network.connect(this.network, mergeItems);

        for (BlockPos connectionPosition : connectionPositions) {
            if (world.getBlockEntity(connectionPosition) instanceof CopperBarrelBlockEntity copperBarrelBlockEntity) {
                copperBarrelBlockEntity.setNetwork(otherCopperBarrelBlockEntity.network);
            }
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof CopperBarrelBlockEntity copperBarrelBlockEntity) {
            if (!copperBarrelBlockEntity.network.isInitialized() && copperBarrelBlockEntity.isController()) {
                copperBarrelBlockEntity.network.initialize(connectionBlockEntity -> {
                    if (connectionBlockEntity instanceof CopperBarrelBlockEntity connectionCopperBarrelBlockEntity) {
                        connectionCopperBarrelBlockEntity.setNetwork(copperBarrelBlockEntity.network);
                    }
                });
            }
        }
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.network.setWorld(world);
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        this.network.disconnect(pos);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.network.canPlayerUse(player);
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
    @Nullable
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CopperBarrelScreenHandler(syncId, playerInventory);
    }

    @Override
    public int size() {
        return this.network.size();
    }

    @Override
    public boolean isEmpty() {
        return this.network.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.network.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return this.network.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return this.network.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.network.setStack(slot, stack);
    }

    @Override
    public void clear() {
        this.network.clear();
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        if (this.isController()) {
            view.read(NETWORK_NAME_KEY, InventoryNetwork.CODEC)
                .ifPresent(network -> {
                    this.network = network;
                    this.network.setWorld(this.world);
                });
        }
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);

        if (this.isController()) {
            view.put(NETWORK_NAME_KEY, InventoryNetwork.CODEC, this.network);
        }
    }
}
