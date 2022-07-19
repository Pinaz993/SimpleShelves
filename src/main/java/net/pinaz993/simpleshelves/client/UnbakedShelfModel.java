package net.pinaz993.simpleshelves.client;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.pinaz993.simpleshelves.SimpleShelves;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * An unbaked model for shelves. Does all the heavy lifting for baking the model. Needs to be implemented by a class
 * that fills in the blanks, like the actual shelf texture and particle texture. That class then can
 */
@Environment(EnvType.CLIENT)
public abstract class UnbakedShelfModel implements UnbakedModel {
    // A sprite ID for the texture used in the book rendering.
    SpriteIdentifier SHELF_BOOKS_ID = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
            new Identifier("simple_shelves:block/shelf_books"));


    // I really wish I could make abstract fields.
    // Alas, an abstract getter works just as well, I suppose.
    // Child objects will need to implement this to tell the system what texture to use for the shelf.
    public abstract SpriteIdentifier getShelfSpriteId();

    public abstract Identifier getShelfModelId();

    @Override
    public  Collection<Identifier> getModelDependencies() {
        return Collections.emptyList(); // We have no model dependencies. Maybe one day I can abstract the actual shelf
                                        // shape to another class, but today is not that day.
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return List.of(
                SHELF_BOOKS_ID,
                getShelfSpriteId()
        );
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
                           ModelBakeSettings rotationContainer, Identifier modelId) {
        // Grab the model for the shelf, including sprites and such. Hang on folks, this is about to get complicated.

        BakedModel shelfModel = loader.getOrLoadModel(getShelfModelId())
                .bake(loader, textureGetter, rotationContainer, getShelfModelId());
        // We need a place to put the models. The BookModelKey class will allow us not only to keep track of them,
        // but also to associate them with their bit mask and book position. Map them together, and we're in business.
        Map<BookModelKey, BakedModel> bookModels = new HashMap<>();
        for(BookModelKey key: BookModelKey.values()) { // Iterate through all book positions.
            // Identifier for grabbing the model from the mod resources.
            Identifier bookModelId = new Identifier(SimpleShelves.NAMESPACE, "block/" + key.MODEL_ID);
            // Grab the model for the book using the vanilla json loading system, and put it into the map.
            bookModels.put(key, loader.getOrLoadModel(bookModelId)
                    .bake(loader, textureGetter, rotationContainer, bookModelId));
        }
        return new BakedShelfModel (bookModels, shelfModel);
    }
}
