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

public class ShelfModel implements UnbakedModel, BakedModel, FabricBakedModel {
    // The collection of all quads that need to be rendered.
    private Mesh mesh;

    @Override
    public Collection<Identifier> getModelDependencies() {
        // TODO: Figure out how to add the empty shelf model here.
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        // TODO: When is this run? At launch, or just before the model is baked?
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
                           ModelBakeSettings rotationContainer, Identifier modelId) {
        // I don't know what any of these things are. Monkey see, Monkey do.
        Renderer renderer = RendererAccess.INSTANCE.getRenderer(); // This smells. Why is there a separate access class?
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        // TODO: Draw the rest of the owl.
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
