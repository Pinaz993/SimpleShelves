package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ShelfModelProvider implements ModelResourceProvider {
    // A list of all the possible models this provider will have to handle.
    // Will need to be reimplemented for any addon mods.
    // Could have just been the list, but I don't mind them being separately referencable like this.
    public static final Identifier ACACIA_SHELF_MODEL_ID = new Identifier("simple_shelves:block/acacia_shelf");
    public static final Identifier BIRCH_SHELF_MODEL_ID = new Identifier("simple_shelves:block/birch_shelf");
    public static final Identifier CRIMSON_SHELF_MODEL_ID = new Identifier("simple_shelves:block/crimson_shelf");
    public static final Identifier DARK_OAK_SHELF_MODEL_ID = new Identifier("simple_shelves:block/dark_oak_shelf");
    public static final Identifier JUNGLE_SHELF_MODEL_ID = new Identifier("simple_shelves:block/jungle_shelf");
    public static final Identifier OAK_SHELF_MODEL_ID = new Identifier("simple_shelves:block/oak_shelf");
    public static final Identifier SPRUCE_SHELF_MODEL_ID = new Identifier("simple_shelves:block/spruce_shelf");
    public static final Identifier WARPED_SHELF_MODEL_ID = new Identifier("simple_shelves:block/warped_shelf");

    public static final List<Identifier> MODEL_IDS = List.of(new Identifier[]{
            ACACIA_SHELF_MODEL_ID, BIRCH_SHELF_MODEL_ID, CRIMSON_SHELF_MODEL_ID, DARK_OAK_SHELF_MODEL_ID,
            JUNGLE_SHELF_MODEL_ID, OAK_SHELF_MODEL_ID, SPRUCE_SHELF_MODEL_ID, WARPED_SHELF_MODEL_ID
    });

    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
        if(MODEL_IDS.contains(resourceId)) // Is this one of the models we handle?
            return new UnbakedShelfModel(); // Return a new shelf model
        else return null; // Nope. Pass.
    }
}
