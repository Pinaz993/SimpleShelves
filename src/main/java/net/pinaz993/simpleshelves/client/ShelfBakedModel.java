package net.pinaz993.simpleshelves.client;


import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class ShelfBakedModel implements BakedModel, FabricBakedModel {
    /*
    * What if I've been trying to do this all wrong? I've been trying to get information from the block entity in here,
    * but what if that's not possible? In that case, block state properties are still my best bet. Also, as far as
    * the randomness of the models go, that's a separate problem I can tackle later. First, I should try to get the
    * model baking working.
    *
    * I do have another idea for that. Perhaps, when the BE is initialized, I can create an integer block state
    * property that is a hash of the position in the world. That can then be used as the seed for any randomness needed
    * to bake the model. From there, it's just setting up a performant and compact way to create the models once and
    * cache them, as well as a performant way to look them up and determine if the work has already been done. A HashSet
    * should work for that, I think.
    *
    *
    * It looks like several of the steps to rendering a specific block that I at first attributed to ShelfUnbakedModel,
    * then to ShelfModelProvider actually belong here. For one, because of FabricBakedModel, it turns out that the only
    * time I can get access to world information for special rendering is in emitBlockQuads(). This means that I need
    * to pass all books into BakedModelInstances, and that I need to choose upon each chunk rebuild which books will be
    * displayed.
    * */

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {

    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return null;
    }

    @Override
    public boolean useAmbientOcclusion() {return false;}

    @Override
    public boolean hasDepth() {return false;}

    @Override
    public boolean isSideLit() {return true;}

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        //TODO: Get sprites.
        return null;
    }

    @Override
    public ModelTransformation getTransformation() {
        return null;
    }

    @Override
    public ModelOverrideList getOverrides() {return null;}

    // No, this shouldn't be put through the vanilla pipeline.
    @Override
    public boolean isVanillaAdapter() {
        return false;
    }
}
