package net.pinaz993.simpleshelves.client;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShelfModelProvider implements ModelResourceProvider {
    /*
    * A lot of the work I was planning on putting in ShelfUnbakedModel.bake() is going to have to go in here. As such,
    * here is a list of features this class will have to implement:
    * * Baked model caching: perhaps using BakedModelCache? I don't know how to compare the baking requirements to the
    *   baked model. Perhaps a random seed that is combined from the world seed and the BlockPos, combined with
    *   the BitFlagContainer's value. That would allow for a hash to be generated that can then be stored as part of a
    *   HashMap. ModelLoader does this using a Triple as one part of the HashMap. I'll have to look up how that works.
    * * */

    // If you wanted to add additional shelves in a dependant mod, you'll need to reimplement this class much as I have.

    // To nip any possible model resource loading conflicts, only these Identifiers will be processed.
    public static final List<Identifier> VALID_SHELF_MODELS = List.of(
            new Identifier("simple_shelves:block/birch_shelf"),
            new Identifier("simple_shelves:block/crimson_shelf"),
            new Identifier("simple_shelves:block/dark_oak_shelf"),
            new Identifier("simple_shelves:block/jungle_shelf"),
            new Identifier("simple_shelves:block/oak_shelf"),
            new Identifier("simple_shelves:block/spruce_shelf"),
            new Identifier("simple_shelves:block/warped_shelf")
    );
    // For all I know, I might be able to rig up data-driven shelves, as the models are fixed, and the only thing that
    // changes is the texture for the model.

    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) {
        return null;
    }
}
