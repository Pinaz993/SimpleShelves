package net.pinaz993.simpleshelves.client;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class ShelfUnbakedModel implements UnbakedModel {
    private static final Identifier ABSTRACT_SHELF_MODEL = new Identifier("simple:shelves:block/abstract_shelf");
    public final Identifier MODEL_ID;
    // The collection of all quads that need to be rendered. Is this needed while baking, or after baking?
    private Mesh mesh;
    // A thing that is important for reasons.
    private ModelTransformation transformation;

    public ShelfUnbakedModel(Identifier id) {
        MODEL_ID = id;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of(
                new Identifier("simple_shelves:abstract_shelf")
        );
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        //TODO: Add in the proper texture from the type of shelf. Does that mean I need another model for each shelf type?
        return BookModel.getAllSpriteIdentifiers();
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
                           ModelBakeSettings rotationContainer, Identifier modelId) {
        // Plan is as follows:
        // 1. Check to see if I can just return the cached model.
        // 2. Apply Rotation
        // 3. Render the shelf using the proper texture.
        // 4. Iterate through all book positions and associated models and render whichever books need to be rendered.
        // 5....
        // 6. Profit

        // I've been thinking about baking, and I think it basically comes down to defining what quads (normally triangles)
        // need to be rendered, how they are positioned, what texture they use, and where on the texture to copy from.
        // I've tackled that in BookModel, so I don't think it'll be too difficult. however, I don't like the idea that
        // I'm going to have to hard code all the models. That way lies misery. I'd like to take advantage of the
        // vanilla system of rendering JSON defined models, but I don't think that I'll be able to. In addition, I don't
        // know enough to be able to engineer a solution that allows data-driven books to be rendered with the
        // randomization that I'm trying to put into the mod. Who knows. Maybe I'll go through all this trouble and
        // it'll look like crap anyway.

        // I need something called a transformation here. I think it has to do with the angle the item displays as in
        // an inventory. Split into two lines like this because there's no way to cast in-line. Java is dumb.
        JsonUnbakedModel defaultShelfModel = (JsonUnbakedModel) loader.getOrLoadModel(ABSTRACT_SHELF_MODEL);
        transformation = defaultShelfModel.getTransformations();
        // I don't know what any of these things are. Monkey see, Monkey do.
        Renderer renderer = RendererAccess.INSTANCE.getRenderer(); // This smells. Why is there a separate access class?
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter(); // Actually, I know what to do with this one. I feed it point and UV
        // coordinates.

        // TODO: Draw the rest of the owl.
    }
}
