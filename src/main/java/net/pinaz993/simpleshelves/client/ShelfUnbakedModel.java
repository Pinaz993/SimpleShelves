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
    //TODO: Separate into UnbakedShelfModel and BakedShelfModel, because I don't like code soup.

    private static final Identifier ABSTRACT_SHELF_MODEL = new Identifier("simple:shelves:block/abstract_shelf");
    public final Identifier MODEL_ID;
    // The collection of all quads that need to be rendered.
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
        return BookModel.getAllSpriteIdentifiers();
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
                           ModelBakeSettings rotationContainer, Identifier modelId) {
        // I need something called a transformation here. I think it has to do with the angle the item displays as in
        // an inventory. Split into two lines like this because there's no way to cast in-line. Java is dumb.
        JsonUnbakedModel defaultBlockModel = (JsonUnbakedModel) loader.getOrLoadModel(ABSTRACT_SHELF_MODEL);
        transformation = defaultBlockModel.getTransformations();
        // I don't know what any of these things are. Monkey see, Monkey do.
        Renderer renderer = RendererAccess.INSTANCE.getRenderer(); // This smells. Why is there a separate access class?
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        // TODO: Draw the rest of the owl.
        // Bake just needs to return a Baked Model. That's literally the only thing it needs to do. This may be simpler
        // than I thought.
    }
}
