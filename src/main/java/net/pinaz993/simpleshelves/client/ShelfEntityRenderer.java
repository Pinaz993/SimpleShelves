package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.HorizontalFacingBlock;
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

@Environment(EnvType.CLIENT)
public class ShelfEntityRenderer implements BlockEntityRenderer<ShelfEntity> {

    @Override
    public void render(ShelfEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if(!entity.hasGenericItems()) return; // If the shelf doesn't have a generic item in it, let's not go through all this trouble.
        World world = entity.getWorld();
        // Check to make sure world isn't null (for some reason), and check to make sure that the block entity isn't
        // lingering in memory after the block has been broken or replaced. Because that's a thing.
        if(world != null && world.getBlockEntity(entity.getPos()) == entity){
            Quaternion rotationQuaternion; // This defines the rotation we use to make sure the items end up in the right places.
            Vec3f axis = new Vec3f(0, 1, 0); // Let's define this once, so we don't waste time and memory.
            switch (world.getBlockState(entity.getPos()).get(HorizontalFacingBlock.FACING)) { // Which direction?
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
        // Rotate to make the stack matches with the direction the shelf is facing.
        matrices.multiply(rotationQuaternion);
        matrices.translate(-.5, -.5, -.5); // Move back to 0,0,0.
        matrices.translate(x, y, .25); // Translate to the position this quadrant occupies.
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer(); // Grab the item renderer. (SINGLETON!)
        // If this is a block that renders as a block:
        if(renderer.getModel(stack, null, null, 0).hasDepth())
            matrices.scale(.75f, .75f, .75f); // Scale to 75%.
        else matrices.scale(.375f, .375f, .375f); // Otherwise scale to 35.7%
        // Rotate 180 degrees to make block or item face properly outward.
        matrices.multiply(new Quaternion(new Vec3f(0, 1, 0), 180, true));
        // Render the item as if it is in an item frame.
        renderer.renderItem(MinecraftClient.getInstance().player, stack, ModelTransformation.Mode.FIXED, false, matrices, vertexConsumers, null, light, overlay, 0);
        matrices.pop(); // We're done with this matrix entry (whatever that entails).
    }
}
