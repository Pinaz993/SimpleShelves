package net.pinaz993.simpleshelves.client;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("deprecation") //Because Mojang abuses @Deprecated. Not the smartest practice, I'll say. Again.
public class ShelfModel implements UnbakedModel, BakedModel, FabricBakedModel {

    // TODO: I still don't know what a mesh is.
    private Mesh mesh;

    @Override
    public Collection<Identifier> getModelDependencies() {
        // TODO: Figure out how to add the empty shelf model here.
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        // TODO: Grab texture dependencies from constituent BookModels.
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        // TODO: Get the textures
        // I don't know what any of these things are. Monkey see, Monkey do.
        Renderer renderer = RendererAccess.INSTANCE.getRenderer(); // This smells. Why is there a separate access class?
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        // TODO: Draw the rest of the owl.
        // Shelf Entity now has a way to get which books need rendered, so I won't need access to world. At this time,
        // I need to determine (via random choice) what textures are getting applied to what books. I also need to
        // remember that the more I can cache (such as the randomized sizes and textures of the books), the less will
        // need to be calculated when it's time to bake the model, and thus the faster baking the model will be.
        // Perhaps I can accomplish that with a field that is calculated if it doesn't exist. However, I don't know if
        // there will be one of these objects for each block. Probably not. I certainly don't have a constructor I can
        // define it in. Maybe that definition should be pushed into the entity after all. It'll have the world
        // coordinates, and it's guaranteed to be one per shelf and generated when the shelf is placed.

        // Alright, that's decided. Then I will need to pass a custom object at baking time that gives the baking
        // process the information it needs to render the books on the shelf. How do I plan on implementing texture
        // fetching? I don't want to use an array, because then I'll be using magic numbers. I need to use a Map of some
        // description. HashMap it is. I'm not too crazy about deciding on the texture for each book in a massive if
        // table inside of a for loop, but I don't have any better ideas. Looks like that's going to be the way to go.

        // It just occurred to me that in theory, this could allow for texture assignment based on NBT data, since I'm
        // handling that in the entity instead of here. That could be really nice. However, there's no way I'm doing the
        // texture work for that. Nope. Nuh uh.

        // It also occurred to me that I'm going to need a way to tell this class that the model needs re-baked, because
        // I'd rather not re-bake it every time someone changes a block in the chunk. I'll need to cache the baked model
        // and re-bake it only if the block has been marked dirty since the last time it was baked.

        // I will NOT be randomizing the widths of the books. Those are going to stay static, not only because I'd have
        // to figure out how to make sure the books in each quadrant added up to eight pixels, but because the clicking
        // positions for inserting and extracting books are hard coded into BookPosition. However, other values for the
        // books, such as how deep and tall they are, can be randomized for aesthetics. That will have to be calulated
        // in the custom class as well as the textures.

    }

    //<editor-fold desc="Puzzling Boilerplate">
    // TODO: Figure out if I need to implement all three classes.
    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
        return null; // "Don't need because we use FabricBakedModel instead" (Press X to doubt).
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true; // "we want the block to have a shadow depending on the adjacent blocks" Fine.
    }

    @Override
    public boolean isBuiltin() { // Elaborate, please.
        return false;
    }

    @Override
    public boolean hasDepth() { // "                 "
        return false;
    }

    @Override
    public boolean isSideLit() { // "                 "
        return false;
    }

    @Override
    public ModelTransformation getTransformation() { // Yet again, I need more information.
        return null;
    }

    @Override
    public ModelOverrideList getOverrides() { //       "                                   "
        return null;
    }
    //</editor-fold>
}
