package net.pinaz993.simpleshelves.client;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShelfModelProvider implements ModelResourceProvider {
    // If you wanted to add additional shelves in a dependant mod, you'll need to reimplement this class much as I have.

    // I don't like doing this manually, but I also don't know if I'm capable of making something do it automatically.
    public static final List<Identifier> VALID_SHELF_MODELS = List.of(
            new Identifier("simple_shelves:block/birch_shelf"),
            new Identifier("simple_shelves:block/crimson_shelf"),
            new Identifier("simple_shelves:block/dark_oak_shelf"),
            new Identifier("simple_shelves:block/jungle_shelf"),
            new Identifier("simple_shelves:block/oak_shelf"),
            new Identifier("simple_shelves:block/spruce_shelf"),
            new Identifier("simple_shelves:block/warped_shelf")
    );

    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) {
        if(VALID_SHELF_MODELS.contains(resourceId)) return new ShelfModel(resourceId);
        else return null;
        }
}
