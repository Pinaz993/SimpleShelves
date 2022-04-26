package net.pinaz993.simpleshelves.client;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public abstract class ShelfUnbakedModel implements UnbakedModel {
    //<editor-fold desc="Static Stuff">
    // Controls how the shelves are rendered in different non-block contexts. Usually defined in JSON.
    private static final ModelTransformation TRANSFORMATION = new ModelTransformation(
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

    public static final List<SpriteIdentifier> SPRITE_IDENTIFIERS = List.of(
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_paper")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_one_px_alpha")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_two_px_alpha")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_two_px_beta")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_two_px_gamma")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_two_px_delta")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_three_px_alpha")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_three_px_beta")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_three_px_gamma")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_three_px_delta")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_three_px_epsilon")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_four_px_alpha")),
            new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("simple_shelves:block/book_four_px_beta"))
    );
    //</editor-fold>

    public final Identifier MODEL_ID, TEXTURE_ID;

    public ShelfUnbakedModel(Identifier modelId, Identifier textureID) {
        MODEL_ID = modelId;
        TEXTURE_ID = textureID;
    }

    //<editor-fold desc="Dependencies">
    @Override
    public Collection<Identifier> getModelDependencies() {
        return null;
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        List<SpriteIdentifier> rtn = new java.util.ArrayList<>(List.copyOf(SPRITE_IDENTIFIERS));
        rtn.add(new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, TEXTURE_ID));
        return rtn;
    }
    //</editor-fold>

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
                           ModelBakeSettings rotationContainer, Identifier modelId) {
        /*
        * Here are some things I've learned from poking about:
        * * I might as well hard code everything, because the books aren't going to be JSON modifiable. Also, who would
        *   want to make custom models for my little mod? It's not as if I'm going to become famous for this.
        * * ShelfModelProvider will have to instantiate each ShelfUnbakedModel instance with references to the proper
        *   block texture.
        * * As it turns out, all books need to be baked into the model, as choosing which books to render can only be
        *   done with world information that can only be obtained in FabricBakedModel.emitBlockQuads(). Thus, here I
        *   only need to provide the quads and textures to the shelf itself, as the book textures will have to be chosen
        *   upon the first chunk rebuild.
        */

        // Here are things I know I'll have to do:

        // Grab Renderer
        Renderer renderer = RendererAccess.INSTANCE.getRenderer(); // This smells. Why is there a separate access class?
        // Grab mesh builder, not sure it does
        MeshBuilder builder = renderer.meshBuilder();
        // Grab quad emitter
        QuadEmitter emitter = builder.getEmitter(); // Actually, I know what to do with this one. I feed it point and UV
        // coordinates.

        // TODO: Figure out how to apply rotations to a model you're baking before you start defining quads.
        // TODO: Implement shelf rendering using data taken from abstract_shelf.json.

        return null;
    }
}
