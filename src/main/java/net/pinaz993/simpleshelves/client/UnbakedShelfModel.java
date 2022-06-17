package net.pinaz993.simpleshelves.client;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

/**
 * An unbaked model for shelves.
 */
@Environment(EnvType.CLIENT)
public class UnbakedShelfModel implements UnbakedModel {

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

    @Override
    public Collection<Identifier> getModelDependencies() {
        return null; // We have no model dependencies. Maybe one day I can abstract the actual shelf shape to another
                     // class, but today is not that day.
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return List.of(
                new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ShelfModelProvider.ACACIA_SHELF_MODEL_ID),
                new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ShelfModelProvider.BIRCH_SHELF_MODEL_ID),
                new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ShelfModelProvider.CRIMSON_SHELF_MODEL_ID),
                new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ShelfModelProvider.DARK_OAK_SHELF_MODEL_ID),
                new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ShelfModelProvider.JUNGLE_SHELF_MODEL_ID),
                new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ShelfModelProvider.OAK_SHELF_MODEL_ID),
                new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ShelfModelProvider.SPRUCE_SHELF_MODEL_ID),
                new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ShelfModelProvider.WARPED_SHELF_MODEL_ID)
        );
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        // I have questions.
        // Do I have to handle rotation here, or can I simply use the block state inherent in horizontal facing block?
        // Can my baked model have a field for its texture, or will that be an issue?
        // How do I cache a baked model? I only need to bake four models per shelf. I don't want to do that over again.

        // It looks like this is the only place I'll be able to get a mesh builder.
        // I think that means that an unbaked model's responsibility is to build meshes. A baked model's responsibility
        // seems to be choosing which mesh to send to the renderer.
        // Because of that, I need to bake the meshes for all 12 books now. This does not bode well for randomizing the
        // appearance of books based on position. I simply won't have that info until FabricBakedModel.emitBlockQuads().
        // One thing is clear. I'm going to need one mesh for the shelf itself, and one for each book.
        return null;
    }
}
