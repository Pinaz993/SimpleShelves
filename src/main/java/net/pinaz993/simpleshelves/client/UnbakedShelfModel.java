package net.pinaz993.simpleshelves.client;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.pinaz993.simpleshelves.BookPosition;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * An unbaked model for shelves. Does all the heavy lifting for baking the model. Needs to be implemented by a class
 * that fills in the blanks, like the actual shelf texture and particle texture. That class then can
 */
@Environment(EnvType.CLIENT)
public abstract class UnbakedShelfModel implements UnbakedModel {

    SpriteIdentifier SHELF_BOOKS_ID = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
            ShelfModelProvider.SHELF_BOOK_TEXTURE_ID);

    // I really wish I could make abstract fields.
    // Alas, an abstract getter works just as well, I suppose.
    public abstract SpriteIdentifier getShelfSpriteID();

    @Override
    public  Collection<Identifier> getModelDependencies() {
        return Collections.emptyList(); // We have no model dependencies. Maybe one day I can abstract the actual shelf
                                        // shape to another class, but today is not that day.
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return List.of(
                SHELF_BOOKS_ID,
                getShelfSpriteID()
        );
    }

    public Mesh buildShelfMesh (MeshBuilder mb, Function<SpriteIdentifier, Sprite> tg){
        Sprite shelfSprite = tg.apply(getShelfSpriteID());
        QuadEmitter e = mb.getEmitter();
        //TODO: Implement shelf model building
        return mb.build();
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        Sprite s = textureGetter.apply(SHELF_BOOKS_ID);
        Renderer ren = RendererAccess.INSTANCE.getRenderer();
        MeshBuilder bui = ren.meshBuilder();
        QuadEmitter emi = bui.getEmitter();

        for(BookQuadEmitter bqe: BookQuadEmitter.class.getEnumConstants()) bqe.emitBookQuads(emi, s);

        return new BakedShelfModel(bui.build(), textureGetter.apply(getShelfSpriteID()));
    }
}
