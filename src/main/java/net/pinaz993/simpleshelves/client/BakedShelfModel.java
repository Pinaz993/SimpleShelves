package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.pinaz993.simpleshelves.BookPosition;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class BakedShelfModel implements FabricBakedModel, BakedModel {
    // Controls how the shelves are rendered in different non-block contexts. Usually defined in JSON.
    //FIXME: Item forms are WAY too offset.
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

    private final Mesh TEMP_BOOK_MESH;
    private final Sprite SHELF_SPRITE;

    //private final Mesh SHELF_MESH;
    //TODO: Implement members for all 12 book meshes.

    public BakedShelfModel(Mesh tbm, Sprite shelfSprite){
        this.SHELF_SPRITE = shelfSprite;
        this.TEMP_BOOK_MESH = tbm;

        //this.SHELF_MESH = sm;


    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        context.meshConsumer().accept(TEMP_BOOK_MESH);

        //FIXME: For now, we're just going to accept all the book meshes, so I can get them all squared away and rendering correctly.


        // int renderData = (int) ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
        // I'll need that later.
        //TODO: Implement bit flag logic, referencing BookPosition bit flags.
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        context.meshConsumer().accept(TEMP_BOOK_MESH);
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
        return SHELF_SPRITE;
    }

    @Override
    public ModelTransformation getTransformation() {
        return TRANSFORMATION;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }
}
