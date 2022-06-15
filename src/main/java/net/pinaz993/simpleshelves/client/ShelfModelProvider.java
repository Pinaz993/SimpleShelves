package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ShelfModelProvider implements ModelResourceProvider {
    // A list of all the possible models this provider will have to handle.
    // Will need to be reimplemented for any addon mods.
    public static final List<Identifier> MODEL_IDS = List.of(new Identifier[]{
            new Identifier("simple_shelves:block/acacia_shelf"),
            new Identifier("simple_shelves:block/birch_shelf"),
            new Identifier("simple_shelves:block/crimson_shelf"),
            new Identifier("simple_shelves:block/dark_oak_shelf"),
            new Identifier("simple_shelves:block/jungle_shelf"),
            new Identifier("simple_shelves:block/oak_shelf"),
            new Identifier("simple_shelves:block/spruce_shelf"),
            new Identifier("simple_shelves:block/warped_shelf")
    });



    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
            if(MODEL_IDS.contains(resourceId)) return new ShelfModel(); // Is the resource ID one that this handles?
            else return null; // Nope. Pass, thanks.
    }
}
