package dev.mariany.copperworks.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InventoryNetwork implements Inventory {
    public static final Codec<InventoryNetwork> CODEC = RecordCodecBuilder.create(
            instance -> instance
                    .group(
                            BlockPos.CODEC.optionalFieldOf("controller_position")
                                          .forGetter(InventoryNetwork::getControllerPos),
                            TextCodecs.CODEC.optionalFieldOf("custom_name")
                                            .forGetter(InventoryNetwork::getCustomName),
                            StackWithSlot.CODEC.listOf()
                                               .fieldOf("held_stacks")
                                               .forGetter(InventoryNetwork::getHeldStacks),
                            BlockPos.CODEC.listOf()
                                          .fieldOf("connections")
                                          .forGetter(InventoryNetwork::getConnections),
                            Codec.INT.fieldOf("slots_per_connection")
                                     .forGetter(InventoryNetwork::getSlotsPerConnection)
                    )
                    .apply(
                            instance,
                            (
                                    pos,
                                    customName,
                                    heldStacks,
                                    connections,
                                    slotsPerConnection
                            ) ->
                                    new InventoryNetwork(
                                            Optional.empty(),
                                            pos,
                                            customName,
                                            heldStacks,
                                            connections,
                                            slotsPerConnection
                                    )
                    )
    );

    public static final PacketCodec<RegistryByteBuf, InventoryNetwork> PACKET_CODEC = PacketCodec.of(
            InventoryNetwork::write,
            InventoryNetwork::read
    );

    public static final PacketCodec<RegistryByteBuf, StackWithSlot> STACK_WITH_SLOT_PACKET_CODEC =
            new PacketCodec<>() {
                public StackWithSlot decode(RegistryByteBuf registryByteBuf) {
                    int slot = registryByteBuf.readInt();
                    ItemStack itemStack = net.minecraft.item.ItemStack.OPTIONAL_PACKET_CODEC.decode(registryByteBuf);

                    return new StackWithSlot(slot, itemStack);
                }

                public void encode(RegistryByteBuf registryByteBuf, StackWithSlot stackWithSlot) {
                    registryByteBuf.writeInt(stackWithSlot.slot());
                    ItemStack.OPTIONAL_PACKET_CODEC.encode(registryByteBuf, stackWithSlot.stack());
                }
            };

    private boolean initialized;

    @Nullable
    protected World world;

    @Nullable
    protected BlockPos controllerPos;

    protected final Map<Integer, ItemStack> heldStacks;
    protected final Set<BlockPos> connections;

    protected final int slotsPerConnection;

    @Nullable
    protected Text customName;

    public InventoryNetwork(@Nullable World world, BlockPos pos, int slotsPerConnection) {
        this(
                Optional.ofNullable(world),
                Optional.of(pos),
                Optional.empty(),
                new ArrayList<>(),
                new ArrayList<>(),
                slotsPerConnection
        );

        this.connections.add(this.controllerPos);
    }

    public InventoryNetwork(
            Optional<World> world,
            Optional<BlockPos> pos,
            Optional<Text> customName,
            List<StackWithSlot> stacksWithSlot,
            List<BlockPos> connections,
            int slotsPerConnection
    ) {
        this.world = world.orElse(null);
        this.controllerPos = pos.orElse(null);
        this.customName = customName.orElse(null);

        this.heldStacks = parseHeldStacks(stacksWithSlot);
        this.connections = new HashSet<>(connections);

        this.slotsPerConnection = slotsPerConnection;
    }

    protected static Map<Integer, ItemStack> parseHeldStacks(List<StackWithSlot> stacksWithSlot) {
        return stacksWithSlot.stream()
                             .collect(Collectors.toMap(StackWithSlot::slot, StackWithSlot::stack));
    }

    public static void write(InventoryNetwork network, RegistryByteBuf buf) {
        BlockPos.PACKET_CODEC.collect(PacketCodecs::optional).encode(
                buf,
                Optional.ofNullable(network.controllerPos)
        );

        TextCodecs.OPTIONAL_PACKET_CODEC.encode(buf, Optional.ofNullable(network.customName));

        STACK_WITH_SLOT_PACKET_CODEC.collect(PacketCodecs.toList()).encode(buf, network.getHeldStacks());

        BlockPos.PACKET_CODEC.collect(PacketCodecs.toList()).encode(buf, network.connections.stream().toList());

        buf.writeInt(network.slotsPerConnection);
    }

    public static InventoryNetwork read(RegistryByteBuf buf) {
        Optional<BlockPos> pos = BlockPos.PACKET_CODEC.collect(PacketCodecs::optional).decode(buf);
        Optional<Text> optionalCustomName = TextCodecs.OPTIONAL_PACKET_CODEC.decode(buf);
        List<StackWithSlot> heldStacks = STACK_WITH_SLOT_PACKET_CODEC.collect(PacketCodecs.toList()).decode(buf);
        List<BlockPos> connections = BlockPos.PACKET_CODEC.collect(PacketCodecs.toList()).decode(buf);
        int slotsPerConnection = buf.readInt();

        return new InventoryNetwork(
                Optional.empty(),
                pos,
                optionalCustomName,
                heldStacks,
                connections,
                slotsPerConnection
        );
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public void initialize(Consumer<BlockEntity> connectionHandler) {
        if (this.world != null) {
            for (BlockPos connectionPos : this.connections) {
                BlockEntity blockEntity = this.world.getBlockEntity(connectionPos);
                connectionHandler.accept(blockEntity);
            }
        }

        this.initialized = true;
    }

    public void setWorld(@Nullable World world) {
        this.world = world;
    }

    public Optional<BlockPos> getControllerPos() {
        return Optional.ofNullable(this.controllerPos);
    }

    private int getSlotsPerConnection() {
        return this.slotsPerConnection;
    }

    public Optional<Text> getCustomName() {
        return Optional.ofNullable(this.customName);
    }

    public void setCustomName(@Nullable Text customName) {
        this.customName = customName;
    }

    protected List<StackWithSlot> getHeldStacks() {
        List<StackWithSlot> stacks = new ArrayList<>();

        for (Map.Entry<Integer, ItemStack> entry : this.heldStacks.entrySet()) {
            int slot = entry.getKey();
            ItemStack stack = entry.getValue();

            if (!stack.isEmpty()) {
                stacks.add(new StackWithSlot(slot, stack));
            }
        }

        return stacks;
    }

    public List<BlockPos> getConnections() {
        return this.connections.stream().toList();
    }

    public void connect(InventoryNetwork otherNetwork, boolean mergeItems) {
        if (otherNetwork != this) {
            this.connections.addAll(otherNetwork.getConnections());

            if (mergeItems) {
                this.heldStacks.putAll(otherNetwork.heldStacks);
            }
        }
    }

    public void disconnect(BlockPos pos) {
        this.connections.remove(pos);

        if (this.world != null) {
            InventoryHelper.scatterItems(this.world, pos, this.adjustSlots());
        }

        this.reassignController();
        this.markDirty();
    }

    private List<ItemStack> adjustSlots() {
        int networkSize = this.size();

        List<ItemStack> overflow = new ArrayList<>();
        List<Integer> outOfBounds = new ArrayList<>();

        for (int slot : this.heldStacks.keySet()) {
            if (slot >= networkSize) {
                outOfBounds.add(slot);
            }
        }

        outOfBounds.sort(Integer::compareTo);

        for (int slot : outOfBounds) {
            ItemStack stack = this.getStack(slot);
            boolean placed = false;

            int newSlot;

            while (true) {
                newSlot = InventoryHelper.shiftLeft(this, slot);

                if (newSlot == slot) {
                    boolean outsideBounds = newSlot >= networkSize;
                    int leftSlot = newSlot - 1;
                    ItemStack leftStack = this.getStack(leftSlot);

                    if (outsideBounds) {
                        placed = false;
                    }

                    if (outsideBounds || leftSlot < 0 || !leftStack.isEmpty()) {
                        break;
                    }
                } else {
                    placed = true;
                    slot = newSlot;
                }
            }

            if (!placed) {
                overflow.add(stack);
            }
        }

        return overflow;
    }

    private void reassignController() {
        if (this.world != null) {
            for (BlockPos pos : this.connections) {
                ChunkPos chunkPos = new ChunkPos(pos);

                if (this.world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
                    this.controllerPos = pos;
                    return;
                }
            }

            this.controllerPos = this.connections.stream().findFirst().orElse(null);
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        double blockInteractionRange = player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE);

        return this.connections
                .stream()
                .anyMatch(pos -> player.canInteractWithBlockAt(pos, blockInteractionRange));
    }

    @Override
    public void markDirty() {
        if (this.world != null && this.controllerPos != null) {
            BlockEntity blockEntity = this.world.getBlockEntity(this.controllerPos);

            if (blockEntity != null) {
                blockEntity.markDirty();
            }
        }
    }

    @Override
    public int size() {
        return this.slotsPerConnection * this.connections.size();
    }

    @Override
    public boolean isEmpty() {
        return this.heldStacks.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.heldStacks.getOrDefault(slot, ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = this.splitStack(slot, amount);

        if (!itemStack.isEmpty()) {
            this.markDirty();
        }

        return itemStack;
    }

    private ItemStack splitStack(int slot, int amount) {
        return !this.getStack(slot).isEmpty() && amount > 0
                ? this.heldStacks.get(slot).split(amount)
                : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = this.heldStacks.getOrDefault(slot, ItemStack.EMPTY);
        this.heldStacks.remove(slot);
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.heldStacks.put(slot, stack);

        if (!stack.isEmpty()) {
            stack.capCount(this.getMaxCount(stack));
        }

        this.markDirty();
    }

    @Override
    public void clear() {
        this.heldStacks.clear();
        this.markDirty();
    }
}
