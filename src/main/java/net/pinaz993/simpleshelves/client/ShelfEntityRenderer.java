package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import net.pinaz993.simpleshelves.ShelfEntity;
import net.pinaz993.simpleshelves.ShelfQuadrant;

public class ShelfEntityRenderer implements BlockEntityRenderer<ShelfEntity> {

    @Environment(EnvType.CLIENT)
    @Override
    public void render(ShelfEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if(!entity.shelfHasGenericItem()) return; // If the shelf doesn't have a generic item in it, let's not go through all this trouble.
        World world = entity.getWorld();
        if(world != null){ // No null pointers allowed here!
            Quaternion rotationQuaternion; // This defines the rotation we use to make sure the items end up in the right places.
            Vec3f axis = new Vec3f(0, 1, 0); // Let's define this once, so we don't waste time and memory.
            switch (world.getBlockState(entity.getPos()).get(HorizontalFacingBlock.FACING)) {
                case NORTH -> rotationQuaternion = new Quaternion(axis, 180, true);
                case EAST -> rotationQuaternion = new Quaternion(axis, 90, true);
                case SOUTH -> rotationQuaternion = new Quaternion(axis, 0, true);
                case WEST -> rotationQuaternion = new Quaternion(axis, 270, true);
                default -> throw new IllegalStateException("Shelves cannot face ".concat( // Let's be verbose about what went wrong.
                        world.getBlockState(entity.getPos()).get(HorizontalFacingBlock.FACING).toString()).concat("."));
            }
            ItemStack stack; // The stack to render, of course.
            // Render ALPHA, iff there is an item there.
            stack = entity.getItems().get(ShelfQuadrant.ALPHA.GENERIC_ITEM_SLOT);
            if (!stack.isEmpty())
                renderStack(matrices, vertexConsumers, light, overlay, stack, rotationQuaternion, .25, .75);
            // Render BETA, iff there is an item there.
            stack = entity.getItems().get(ShelfQuadrant.BETA.GENERIC_ITEM_SLOT);
            if (!stack.isEmpty())
                renderStack(matrices, vertexConsumers, light, overlay, stack, rotationQuaternion, .75, .75);
            // Render GAMMA, iff there is an item there.
            stack = entity.getItems().get(ShelfQuadrant.GAMMA.GENERIC_ITEM_SLOT);
            if (!stack.isEmpty())
                renderStack(matrices, vertexConsumers, light, overlay, stack, rotationQuaternion, .25, .25);
            // Render DELTA, iff there is an item there.
            stack = entity.getItems().get(ShelfQuadrant.DELTA.GENERIC_ITEM_SLOT);
            if (!stack.isEmpty())
                renderStack(matrices, vertexConsumers, light, overlay, stack, rotationQuaternion, .75, .25);
        }
    }

    private void renderStack(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay,
                             ItemStack stack, Quaternion rotationQuaternion, double x, double y)
    {
        matrices.push(); // Obligatory, for OpenGL calls of any kind. I have only vague ideas of why.
        matrices.translate(.5, .5, .5); // Move to the middle of the block before rotating.
        matrices.multiply(rotationQuaternion); // Rotate to make the stack matches with the direction the shelf is facing.
        matrices.translate(-.5, -.5, -.5); // Move back to 0,0,0.
        matrices.translate(x, y, .25); // Translate to the position this quadrant occupies.
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer(); // Grab the renderer from the instance;
        // If this is a block that renders as a block scale to 75%.
        if(renderer.getHeldItemModel(stack, null, null, 0).hasDepth()) matrices.scale(.75f, .75f, .75f);
        else matrices.scale(.375f, .375f, .375f); // Otherwise scale to 35.7%
        renderer.renderItem(stack, ModelTransformation.Mode.FIXED, light, overlay, matrices, vertexConsumers, 0); // Actually render the item.
        matrices.pop(); // We're done with this matrix entry (whatever that entails).
    }
}
