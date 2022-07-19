package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.pinaz993.simpleshelves.SimpleShelves;

@Environment(EnvType.CLIENT)
public class SimpleShelvesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register the block entity renderer for the shelves
        BlockEntityRendererRegistry.register(
                SimpleShelves.SHELF_BLOCK_ENTITY,
                (BlockEntityRendererFactory.Context ctx) -> new ShelfEntityRenderer()
        );
        // Register the model loader for the shelves.
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new ShelfModelProvider());
    }
}
