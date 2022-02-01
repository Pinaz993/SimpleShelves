package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.pinaz993.simpleshelves.SimpleShelves;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class SimpleShelvesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(
                SimpleShelves.SHELF_BLOCK_ENTITY,
                (BlockEntityRendererFactory.Context ctx) -> new ShelfEntityRenderer()
        );
    }
}
