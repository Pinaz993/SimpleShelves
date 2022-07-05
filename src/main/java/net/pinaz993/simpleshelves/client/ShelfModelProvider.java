package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.pinaz993.simpleshelves.SimpleShelves;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ShelfModelProvider implements ModelResourceProvider {
    // A list of all the possible models this provider will have to handle.
    // Will need to be reimplemented for any addon mods.
    // Could have just been the list, but I don't mind them being separately referencable like this.
    public static final ModelIdentifier ACACIA_SHELF_MODEL_ID = new ModelIdentifier("simple_shelves:block/acacia_shelf");
    public static final ModelIdentifier BIRCH_SHELF_MODEL_ID = new ModelIdentifier("simple_shelves:block/birch_shelf");
    public static final ModelIdentifier CRIMSON_SHELF_MODEL_ID = new ModelIdentifier("simple_shelves:block/crimson_shelf");
    public static final ModelIdentifier DARK_OAK_SHELF_MODEL_ID = new ModelIdentifier("simple_shelves:block/dark_oak_shelf");
    public static final ModelIdentifier JUNGLE_SHELF_MODEL_ID = new ModelIdentifier("simple_shelves:block/jungle_shelf");
    public static final ModelIdentifier OAK_SHELF_MODEL_ID = new ModelIdentifier("simple_shelves:block/oak_shelf");
    public static final ModelIdentifier SPRUCE_SHELF_MODEL_ID = new ModelIdentifier("simple_shelves:block/spruce_shelf");
    public static final ModelIdentifier WARPED_SHELF_MODEL_ID = new ModelIdentifier("simple_shelves:block/warped_shelf");
    // An identifier for the texture I use for books.
    public static final Identifier SHELF_BOOK_TEXTURE_ID = new Identifier("simple_shelves:block/shelf_books");

    public static final List<ModelIdentifier> MODEL_IDS = List.of(new ModelIdentifier[]{
            ACACIA_SHELF_MODEL_ID, BIRCH_SHELF_MODEL_ID, CRIMSON_SHELF_MODEL_ID, DARK_OAK_SHELF_MODEL_ID,
            JUNGLE_SHELF_MODEL_ID, OAK_SHELF_MODEL_ID, SPRUCE_SHELF_MODEL_ID, WARPED_SHELF_MODEL_ID
    });

    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
        if (resourceId.getNamespace().equals(SimpleShelves.NAMESPACE)) { // Is the ID from this mod?
            for(ModelIdentifier mi: MODEL_IDS) {// Iterate through all the model IDs
                if (resourceId.getPath().equals(mi.getPath())) // Does the ID in question have the same path as the current model ID?
                    return new UnbakedShelfModel(); // Yes. Return a new shelf model.
                else if (resourceId.getPath().contains("item")) // Does the path indicate that this is an item model?
                    return new UnbakedShelfModel(); // Yes. Return a new shelf model.
            }
        }
        return null; // Nope. Pass.
    }
}
