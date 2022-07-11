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
import net.pinaz993.simpleshelves.client.woodshelves.*;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ShelfModelProvider implements ModelResourceProvider {
    // A list of all the possible models this provider will have to handle.
    // Will need to be reimplemented for any addon mods.
    // Could have just been the list, but I don't mind them being separately referencable like this.
    // An identifier for the texture I use for books.
    public static final Identifier SHELF_BOOK_TEXTURE_ID = new Identifier("simple_shelves:block/shelf_books");

    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
        if (resourceId.getNamespace().equals(SimpleShelves.NAMESPACE)) { // Is the ID from this mod?
            return switch (resourceId.getPath().split("/")[1]) {       // Here, we're grabbing the String 'path'
                case "acacia_shelf" -> new AcaciaShelfModel();         // ("block/oak_shelf") from the resource ID,
                case "birch_shelf" -> new BirchShelfModel();           // which we've already ensured comes from this
                case "crimson_shelf" -> new CrimsonShelfModel();       // mod. Then, we're using regex to split that
                case "dark_oak_shelf" -> new DarkOakShelfModel();      // String into an array of Strings
                case "jungle_shelf"-> new JungleShelfModel();          // {"block", "oak_shelf"}, which we then take the
                case "oak_shelf" -> new OakShelfModel();               // second entry in ("oak_shelf") so that we can
                case "spruce_shelf" -> new SpruceShelfModel();         // see which block/item we need to make a model
                case "warped_shelf" -> new WarpedShelfModel();         // for. If it doesn't fall into one of these
                default -> null;                                       // entries, we return null to let the game know
            };                                                         // to fall back to the vanilla system.
        }
        return null; // Nope. Pass.
    }
}
