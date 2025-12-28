package dev.mariany.copperworks.inventory;

import dev.mariany.copperworks.Copperworks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public interface InventoryNetworkContainer extends Inventory {
    String NETWORK_KEY = Copperworks.id("network").toString();

    InventoryNetwork createNetwork();

    InventoryNetwork getNetwork();

    void setNetwork(InventoryNetwork network);

    default void connect(World world, InventoryNetworkContainer otherNetworkContainer, boolean mergeItems) {
        InventoryNetwork currentNetwork = this.getNetwork();
        InventoryNetwork otherNetwork = otherNetworkContainer.getNetwork();

        if (currentNetwork.equals(otherNetwork)) {
            return;
        }

        List<BlockPos> connectionPositions = currentNetwork.getConnections();

        otherNetwork.connect(currentNetwork, mergeItems);

        for (BlockPos connectionPosition : connectionPositions) {
            BlockEntity blockEntity = world.getBlockEntity(connectionPosition);

            if (blockEntity instanceof InventoryNetworkContainer inventoryNetworkContainer) {
                inventoryNetworkContainer.setNetwork(otherNetwork);
            }
        }
    }

    default void disconnect(BlockPos pos) {
        InventoryNetwork originalNetwork = this.getNetwork();
        Set<InventoryNetwork> newNetworks = new HashSet<>();

        this.disconnect(pos, newNetworks);
        this.handleDisconnectStacks(pos, originalNetwork, newNetworks);
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
                        Copperworks.LOGGER.info(
                                "Found empty slot in network with controller found at {}",
                                network.getControllerPos().orElse(null)
                        );
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

    private void disconnect(BlockPos pos, Set<InventoryNetwork> newNetworks) {
        this.disconnect(pos, new HashSet<>(), newNetworks);
    }

    private InventoryNetwork disconnect(
            BlockPos pos,
            Set<BlockPos> verifiedPositions,
            Set<InventoryNetwork> newNetworks
    ) {
        InventoryNetwork newNetwork = this.createNetwork();
        this.getNetwork().disconnect(pos, verifiedPositions, newNetworks);
        this.setNetwork(newNetwork);
        return newNetwork;
    }

    static void validateConnection(
            InventoryNetworkContainer networkContainer,
            BlockPos pos,
            Set<BlockPos> verifiedPositions,
            Set<InventoryNetwork> newNetworks
    ) {
        InventoryNetwork network = networkContainer.getNetwork();

        network.getWorld().ifPresent(world -> {
            if (!verifiedPositions.contains(pos)) {
                network.getControllerPos().ifPresent(controllerPos -> {
                    boolean isReachable = isReachable(
                            pos,
                            controllerPos,
                            (walkPos -> {
                                BlockEntity blockEntity = world.getBlockEntity(walkPos);

                                return blockEntity instanceof InventoryNetworkContainer walkedNetworkContainer &&
                                        walkedNetworkContainer.getNetwork().equals(network);
                            })
                    );

                    verifiedPositions.add(pos);

                    if (!isReachable) {
                        newNetworks.add(networkContainer.disconnect(pos, verifiedPositions, newNetworks));

                        for (Direction direction : Direction.values()) {
                            BlockEntity blockEntity = world.getBlockEntity(pos.offset(direction));

                            if (blockEntity instanceof InventoryNetworkContainer offsetNetworkContainer) {
                                InventoryNetwork offsetNetwork = offsetNetworkContainer.getNetwork();

                                if (!offsetNetwork.equals(network)) {
                                    networkContainer.connect(world, offsetNetworkContainer, true);
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    private static boolean isReachable(
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

    static boolean isController(InventoryNetworkContainer otherNetworkContainer, BlockPos pos) {
        return otherNetworkContainer.getNetwork().getControllerPos()
                                    .map(controllerPos -> controllerPos.equals(pos))
                                    .orElse(false);
    }

    static void tick(BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getPos();

        if (blockEntity instanceof InventoryNetworkContainer inventoryNetworkContainer) {
            InventoryNetwork network = inventoryNetworkContainer.getNetwork();
            boolean isInitialized = network.isInitialized();
            boolean isController = InventoryNetworkContainer.isController(inventoryNetworkContainer, pos);

            if (!isInitialized && isController) {
                network.initialize();
            }
        }
    }

    static void onRemoved(InventoryNetworkContainer networkContainer, BlockPos pos) {
        networkContainer.getNetwork().getWorld().ifPresent(world -> {
            if (world instanceof ServerWorld serverWorld && !serverWorld.getServer().isStopped()) {
                networkContainer.disconnect(pos);
            }
        });
    }

    static void readData(
            ReadView view,
            InventoryNetworkContainer inventoryNetworkContainer,
            @Nullable World world,
            BlockPos pos
    ) {
        if (InventoryNetworkContainer.isController(inventoryNetworkContainer, pos)) {
            view.read(NETWORK_KEY, InventoryNetwork.CODEC)
                .ifPresent(network -> {
                    network.setWorld(world);
                    inventoryNetworkContainer.setNetwork(network);
                });
        }
    }

    static void writeData(WriteView view, InventoryNetworkContainer inventoryNetworkContainer, BlockPos pos) {
        if (InventoryNetworkContainer.isController(inventoryNetworkContainer, pos)) {
            view.put(NETWORK_KEY, InventoryNetwork.CODEC, inventoryNetworkContainer.getNetwork());
        }
    }

    static void readComponents(ComponentsAccess components, InventoryNetworkContainer inventoryNetworkContainer) {
        Text customName = components.get(DataComponentTypes.CUSTOM_NAME);

        if (customName != null) {
            inventoryNetworkContainer.getNetwork().setCustomName(customName);
        }
    }

    static void addComponents(ComponentMap.Builder builder, InventoryNetworkContainer inventoryNetworkContainer) {
        inventoryNetworkContainer.getNetwork()
                                 .getCustomName()
                                 .ifPresent(customName -> builder.add(DataComponentTypes.CUSTOM_NAME, customName));
    }

    static void removeFromCopiedStackData(WriteView view) {
        view.remove(NETWORK_KEY);
    }

    @Override
    default int size() {
        return this.getNetwork().size();
    }

    @Override
    default boolean isEmpty() {
        return this.getNetwork().isEmpty();
    }

    @Override
    default ItemStack getStack(int slot) {
        return this.getNetwork().getStack(slot);
    }

    @Override
    default ItemStack removeStack(int slot, int amount) {
        return this.getNetwork().removeStack(slot, amount);
    }

    @Override
    default ItemStack removeStack(int slot) {
        return this.getNetwork().removeStack(slot);
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
        this.getNetwork().setStack(slot, stack);
    }

    @Override
    default void clear() {
        this.getNetwork().clear();
    }

    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return this.getNetwork().canPlayerUse(player);
    }
}
