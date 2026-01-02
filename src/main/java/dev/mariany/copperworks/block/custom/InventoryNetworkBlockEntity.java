package dev.mariany.copperworks.block.custom;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.inventory.InventoryHelper;
import dev.mariany.copperworks.inventory.InventoryNetwork;
import dev.mariany.copperworks.inventory.InventoryNetworkState;
import dev.mariany.copperworks.packet.clientbound.InventoryNetworkUpdatePacket;
import dev.mariany.copperworks.screen.InventoryNetworkScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
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

import java.util.*;
import java.util.function.Predicate;

public abstract class InventoryNetworkBlockEntity extends BlockEntity
        implements Inventory, NamedScreenHandlerFactory, Nameable {
    protected final String NETWORK_KEY = Copperworks.id("network").toString();

    protected final Text defaultName;
    protected final int slotsPerConnection;
    protected InventoryNetwork network;

    public InventoryNetworkBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            int slotsPerConnection,
            Text defaultName
    ) {
        super(type, pos, state);
        this.defaultName = defaultName;
        this.slotsPerConnection = slotsPerConnection;
        this.network = this.createNetwork();
    }

    public InventoryNetwork getNetwork() {
        return this.network;
    }

    public void setNetwork(InventoryNetwork network) {
        this.network = network;
    }

    public void interact(PlayerEntity player, BlockPos pos) {
        if (player.getEntityWorld() instanceof ServerWorld serverWorld) {
            GlobalPos globalPos = GlobalPos.create(serverWorld.getRegistryKey(), pos);

            if (player instanceof InventoryNetworkState networkState) {
                networkState.copperworks2$setNetwork(this.network);
            }

            if (player instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new InventoryNetworkUpdatePacket(globalPos, this.network));
            }

            PiglinBrain.onGuardedBlockInteracted(serverWorld, player, true);
        }
    }

    public void tick() {
        if (!this.network.isInitialized() && this.isController()) {
            this.network.initialize();
        }
    }

    protected boolean isController() {
        return this.network.getControllerPos()
                           .map(controllerPos -> controllerPos.equals(this.pos))
                           .orElse(false);
    }

    public void validateConnection(BlockPos pos, Set<BlockPos> verifiedPositions, Set<InventoryNetwork> newNetworks) {
        this.network.getWorld().ifPresent(world -> {
            if (!verifiedPositions.contains(this.pos)) {
                this.network.getControllerPos().ifPresent(controllerPos -> {
                    boolean isReachable = isReachable(
                            this.pos,
                            controllerPos,
                            walkPos -> {
                                BlockEntity blockEntity = world.getBlockEntity(walkPos);

                                return blockEntity instanceof InventoryNetworkBlockEntity walkedNetworkBlockEntity &&
                                        walkedNetworkBlockEntity.network.equals(this.network);
                            }
                    );

                    verifiedPositions.add(pos);

                    if (!isReachable) {
                        newNetworks.add(this.disconnect(pos, verifiedPositions, newNetworks));

                        for (Direction direction : Direction.values()) {
                            BlockEntity blockEntity = world.getBlockEntity(pos.offset(direction));

                            if (blockEntity instanceof InventoryNetworkBlockEntity offsetInventoryNetworkBlockEntity) {
                                if (!offsetInventoryNetworkBlockEntity.network.equals(this.network)) {
                                    this.connect(world, offsetInventoryNetworkBlockEntity, true);
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    protected static boolean isReachable(
            BlockPos start,
            BlockPos end,
            Predicate<BlockPos> predicate
    ) {
        if (start.equals(end)) {
            return true;
        }

        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();

            for (Direction direction : Direction.values()) {
                BlockPos offsetPos = pos.offset(direction);

                if (visited.contains(offsetPos)) {
                    continue;
                }

                if (!predicate.test(offsetPos)) {
                    continue;
                }

                if (offsetPos.equals(end)) {
                    return true;
                }

                queue.add(offsetPos);
                visited.add(offsetPos);
            }
        }

        return false;
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
    public boolean canPlayerUse(PlayerEntity player) {
        return this.network.canPlayerUse(player);
    }

    @Override
    public Text getName() {
        return this.network.getCustomName().orElse(this.defaultName);
    }

    @Override
    public Text getDisplayName() {
        return this.getName();
    }

    @Override
    @Nullable
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new InventoryNetworkScreenHandler(syncId, playerInventory);
    }

    @Override
    public void clear() {
        this.network.clear();
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.network.setWorld(world);
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        this.onRemoved();
    }

    protected void onRemoved() {
        this.network.getWorld().ifPresent(world -> {
            if (world instanceof ServerWorld serverWorld && !serverWorld.getServer().isStopped()) {
                this.disconnect();
            }
        });
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        // Prevents default item scatter behavior
    }

    public void onBlockAdded(World world, BlockPos pos) {
        Set<BlockPos> discoveredControllers = new HashSet<>();

        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos.offset(direction);
            BlockEntity neighboringBlockEntity = world.getBlockEntity(offsetPos);

            if (neighboringBlockEntity instanceof InventoryNetworkBlockEntity inventoryNetworkBlockEntity) {
                InventoryNetwork network = inventoryNetworkBlockEntity.getNetwork();

                network.getControllerPos().ifPresent(controllerPos -> {
                    this.connect(world, inventoryNetworkBlockEntity, !discoveredControllers.contains(controllerPos));
                    discoveredControllers.add(controllerPos);
                });
            }
        }
    }

    protected void connect(
            World world,
            InventoryNetworkBlockEntity otherInventoryNetworkBlockEntity,
            boolean mergeItems
    ) {
        InventoryNetwork otherNetwork = otherInventoryNetworkBlockEntity.network;

        if (this.network.equals(otherNetwork)) {
            return;
        }

        List<BlockPos> connectionPositions = this.network.getConnections();

        otherNetwork.connect(this.network, mergeItems);

        for (BlockPos connectionPosition : connectionPositions) {
            BlockEntity blockEntity = world.getBlockEntity(connectionPosition);

            if (blockEntity instanceof InventoryNetworkBlockEntity inventoryNetworkBlockEntity) {
                inventoryNetworkBlockEntity.setNetwork(otherNetwork);
            }
        }
    }

    protected void disconnect() {
        InventoryNetwork originalNetwork = this.network;
        Set<InventoryNetwork> newNetworks = new HashSet<>();

        this.disconnect(this.pos, newNetworks);
        this.handleDisconnectStacks(this.pos, originalNetwork, newNetworks);
    }

    protected void disconnect(BlockPos pos, Set<InventoryNetwork> newNetworks) {
        this.disconnect(pos, new HashSet<>(), newNetworks);
    }

    protected InventoryNetwork disconnect(
            BlockPos pos,
            Set<BlockPos> verifiedPositions,
            Set<InventoryNetwork> newNetworks
    ) {
        InventoryNetwork newNetwork = this.createNetwork();
        this.network.disconnect(pos, verifiedPositions, newNetworks);
        this.network = newNetwork;
        return newNetwork;
    }

    private void handleDisconnectStacks(
            BlockPos pos,
            InventoryNetwork originalNetwork,
            Set<InventoryNetwork> newNetworks
    ) {
        List<ItemStack> overflowingStacks = new ArrayList<>();

        List<InventoryNetwork> allNetworks = new ArrayList<>(newNetworks);
        allNetworks.add(originalNetwork);

        for (InventoryNetwork network : allNetworks) {
            overflowingStacks.addAll(network.adjustSlots());
        }

        Set<Integer> placedStackIndexes = new HashSet<>();
        List<InventoryNetwork> validNetworks = new ArrayList<>(
                newNetworks.stream().filter(InventoryNetwork::isValid).toList()
        );

        validNetworks.sort(Comparator.comparing(InventoryNetwork::size).reversed());

        for (int stackIndex = 0; stackIndex < overflowingStacks.size(); stackIndex++) {
            ItemStack overflowingStack = overflowingStacks.get(stackIndex);

            NETWORK_LOOP:
            for (InventoryNetwork network : validNetworks) {
                for (int networkIndex = 0; networkIndex < network.size(); networkIndex++) {
                    ItemStack networkStack = network.getStack(networkIndex);

                    if (networkStack.isEmpty()) {
                        network.setStack(networkIndex, overflowingStack.copy());
                        placedStackIndexes.add(stackIndex);
                        break NETWORK_LOOP;
                    }
                }
            }
        }

        List<Integer> sortedPlacedStackIndexes = new ArrayList<>(placedStackIndexes);
        sortedPlacedStackIndexes.sort(Comparator.reverseOrder());

        for (int index : sortedPlacedStackIndexes) {
            overflowingStacks.remove(index);
        }

        originalNetwork.getWorld().ifPresent(
                world -> InventoryHelper.scatterItems(world, pos, overflowingStacks)
        );
    }

    protected InventoryNetwork createNetwork() {
        return new InventoryNetwork(this.world, this.pos, this.slotsPerConnection);
    }

    @Override
    public void removeFromCopiedStackData(WriteView view) {
        view.remove(NETWORK_KEY);
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);

        Text customName = components.get(DataComponentTypes.CUSTOM_NAME);

        if (customName != null) {
            this.network.setCustomName(customName);
        }
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);

        this.network.getCustomName().ifPresent(
                customName -> builder.add(DataComponentTypes.CUSTOM_NAME, customName)
        );
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        if (this.isController()) {
            view.read(NETWORK_KEY, InventoryNetwork.CODEC).ifPresent(network -> {
                network.setWorld(world);
                this.network = network;
            });
        }
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);

        if (this.isController()) {
            view.put(NETWORK_KEY, InventoryNetwork.CODEC, this.network);
        }
    }
}
