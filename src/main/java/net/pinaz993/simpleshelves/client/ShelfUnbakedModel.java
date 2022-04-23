package net.pinaz993.simpleshelves.client;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public abstract class ShelfUnbakedModel implements UnbakedModel {
    private static final Identifier ABSTRACT_SHELF_MODEL = new Identifier("simple:shelves:block/abstract_shelf");
    public final Identifier MODEL_ID;
    // The collection of all quads that need to be rendered. Is this needed while baking, or after baking?
    private Mesh mesh;

    // Controls how the shelves are rendered in different non-block contexts. Usually defined in JSON.
    private final ModelTransformation TRANSFORMATION = new ModelTransformation(
            new Transformation( // 3PP Left Hand
                    new Vec3f(75f, -45f, 0f),
                    new Vec3f(0f, 2.5f, 0),
                    new Vec3f(.375f, .375f, .375f)
            ),
            new Transformation( // 3PP Right Hand
                    new Vec3f(75f, -45f, 0f),
                    new Vec3f(0f, 2.5f, 0),
                    new Vec3f(.375f, .375f, .375f)
            ),
            new Transformation( // 1PP Left Hand
                    new Vec3f(0f, 45f, 0f),
                    new Vec3f(0f, 2.5f, 0),
                    new Vec3f(.4f, .4f, .4f)
            ),
            new Transformation( // 1PP Left Hand
                    new Vec3f(0f, 45f, 0f),
                    new Vec3f(0f, 2.5f, 0),
                    new Vec3f(.4f, .4f, .4f)
            ),
            new Transformation( // Head
                    new Vec3f(0f, 0f, 0f),
                    new Vec3f(0f, 0f, .5f),
                    new Vec3f(1f, 1f, 1f)
            ),
            new Transformation( // GUI
                    new Vec3f(30f, 135f, 0f),
                    new Vec3f(-1.5f, -1f, 0f),
                    new Vec3f(.625f, .625f, .625f)
            ),
            new Transformation( // Ground
                    new Vec3f(0f, 0f, 0f),
                    new Vec3f(0f, 3f, 0f),
                    new Vec3f(.25f, .25f, .25f)
            ),
            new Transformation( // Fixed, or Item Frame
                    new Vec3f(0f, 0f, 0f),
                    new Vec3f(0f, 0f, -4f),
                    new Vec3f(.5f, .5f, .5f)
            )
    );

    public ShelfUnbakedModel(Identifier id) {
        MODEL_ID = id;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of(
                new Identifier("simple_shelves:abstract_shelf")
        );
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
                           ModelBakeSettings rotationContainer, Identifier modelId) {
        /*
        * I can't exactly plan this out right now. There are some serious issues with the process. I think I'm going to
        * need a custom ModelResourceProvider, as this bake method does NOT have a way to get block entity data, nor a way to
        * receive it, short of something unconventional like slipping it into modelId. That being said, here are some
        * things I've learned from poking about:
        * * ModelTransformation instances contain the data needed to display the block as an item in a frame, on an
        *   entities head, or otherwise. I'll need to statically define that, because...
        * * I might as well hard code everything, because the books aren't going to be JSON modifiable. Also, who would
        *   want to make custom models for my little mod? It's not as if I'm going to become famous for this.
        * * It looks like there'll be a way to use modelId to dynamically figure out what texture to use, though I think
        *   that part WILL involve JSON. I'll just need to use a JSON deserializer to get the texture ID for the shelf,
        *   and hopefully that'll be enough. Who knows? Maybe this will lead to data-driven shelves!
        */

        // I've been thinking about baking, and I think it basically comes down to defining what quads (normally triangles)
        // need to be rendered, how they are positioned, what texture they use, and where on the texture to copy from.
        // I've tackled that in BookModel, so I don't think it'll be too difficult.

        // Here are things I know I'll have to do:

        // I don't know what any of these things are. Monkey see, Monkey do.
        Renderer renderer = RendererAccess.INSTANCE.getRenderer(); // This smells. Why is there a separate access class?
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter(); // Actually, I know what to do with this one. I feed it point and UV
        // coordinates.

        // TODO: Draw the rest of the owl.
    }
}
