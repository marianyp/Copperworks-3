package dev.mariany.copperworks.client.render.block.entity;

import dev.mariany.copperworks.block.custom.relay.BoundRelayBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class BoundRelayBlockEntityRenderer extends RelayBlockEntityRenderer<BoundRelayBlockEntity> {
    public BoundRelayBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected Optional<BlockPos> getBoundPosition(BoundRelayBlockEntity blockEntity) {
        return blockEntity.getBoundPos().map(boundPos -> {
            RegistryKey<World> dimension = boundPos.dimension();
            ClientWorld world = MinecraftClient.getInstance().world;

            if (world != null) {
                if (dimension.equals(world.getRegistryKey())) {
                    return boundPos.pos();
                }
            }

            return null;
        });
    }
}
