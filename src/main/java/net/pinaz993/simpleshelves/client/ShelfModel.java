package net.pinaz993.simpleshelves.client;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ShelfModel implements UnbakedModel, BakedModel, FabricBakedModel {
    // The collection of all quads that need to be rendered.
    private static final Identifier DEFAULT_BLOCK_MODEL = new Identifier("minecraft:block/block");
    private Mesh mesh;
    private ModelTransformation transformation;

    @Override
    public Collection<Identifier> getModelDependencies() {
        return List.of(DEFAULT_BLOCK_MODEL);

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
        // I need something called a transformation here. I think it has to do with the angle the item displays as in
        // an inventory. Split into two lines like this because there's no way to cast in-line. Java is dumb.
        JsonUnbakedModel defaultBlockModel = (JsonUnbakedModel) loader.getOrLoadModel(DEFAULT_BLOCK_MODEL);
        transformation = defaultBlockModel.getTransformations();
        // I don't know what any of these things are. Monkey see, Monkey do.
        Renderer renderer = RendererAccess.INSTANCE.getRenderer(); // This smells. Why is there a separate access class?
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        // TODO: Draw the rest of the owl.
    }

    //<editor-fold desc="Puzzling Boilerplate">
    // TODO: Figure out if I need to implement all these classes.
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
    public Sprite getSprite() {
        return null;
    }

    // I have no clue what this is for.
    @Override
    public boolean hasDepth() {return false;}

    // Shelves are blocks, so render them as such in the inventory.
    @Override
    public boolean isSideLit() {return true;}

    // I think this tells MC to render this the same as any block when in item mode.
    @Override
    public ModelTransformation getTransformation() {return transformation;}

    @Override
    public ModelOverrideList getOverrides() { //       "                                   "
        return null;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {

    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

    }
    //</editor-fold>
}
