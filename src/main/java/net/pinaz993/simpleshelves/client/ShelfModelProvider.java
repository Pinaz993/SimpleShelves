package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import net.pinaz993.simpleshelves.SimpleShelves;
import net.pinaz993.simpleshelves.client.woodshelves.*;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ShelfModelProvider implements ModelResourceProvider {
    /**
     * Returns a new model object if the resource ID is a: from this mod and b: of a type that this provider handles.
     * For anyone looking to make an addon mod to Simple Shelves, you'll need to do the same. I recommend the following
     * procedure:
     * 1. Check to see if the resource ID is from your mod. Do this first so that it can fail fast on the literally
     * thousands of vanilla models that are going to be run through this method, besides any modded models needed.
     *
     * 2. Check the path part of the ID to see which block or item it's supposed to be applying to. Below, I'm using the
     * '/' character to cut away the part of the ID that indicates if the ID is for a block or an item. I do that
     * because the only blocks or items that I need rendered are all shelves, and they will be using the same model as
     * both blocks and items. You might need to handle things differently if you have an item with a custom model.
     * The string values I chose are taken from the resource Identifiers I defined in my main class, SimpleShelves.
     *
     * Keep in mind that even though you've taken care of the model IDs here, you'll still need a blockstate json
     * model for each block you define. It'll point to the model identifier that you're filtering for here.
     * See src/main/resources/assets/simple_shelves/blockstates to see how I did that.
     */
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
