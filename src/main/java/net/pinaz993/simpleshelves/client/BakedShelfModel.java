package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class BakedShelfModel implements FabricBakedModel, BakedModel {
    private final Map<BookModelKey, BakedModel> BOOK_MODELS; // All 12 models, wrapped snugly in a map.
    private final BakedModel SHELF_MODEL; // The model for the shelf when empty.

    public BakedShelfModel(Map<BookModelKey, BakedModel> bookModels, BakedModel shelfModel){
        this.SHELF_MODEL = shelfModel;
        this.BOOK_MODELS = bookModels;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        context.fallbackConsumer().accept(SHELF_MODEL); // Always render the shelf, no matter what is or isn't on  it.
        for(BookModelKey key: BOOK_MODELS.keySet()) {
            BakedModel model = BOOK_MODELS.get(key);
            if(true) {//TODO: Implement bit mask logic. Need to do so in the entity as well.
                context.fallbackConsumer().accept(model);
            }
        }


        // int renderData = (int) ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
        // I'll need that later.

    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        context.fallbackConsumer().accept(SHELF_MODEL);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean hasDepth() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return SHELF_MODEL.getParticleSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        // Controls how the shelves are rendered in different non-block contexts. Usually defined in JSON.
        return SHELF_MODEL.getTransformation();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }
}
