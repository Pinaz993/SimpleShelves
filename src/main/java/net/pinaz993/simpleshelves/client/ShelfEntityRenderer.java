package net.pinaz993.simpleshelves.client;

import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.pinaz993.simpleshelves.ShelfEntity;

public class ShelfEntityRenderer implements BlockEntityRenderer<ShelfEntity> {

    @Override
    public void render(ShelfEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        if(entity.getWorld() != null){
            matrices.translate(.5, .5, .5);
            switch (entity.getWorld().getBlockState(entity.getPos()).get(HorizontalFacingBlock.FACING)) {
                case NORTH -> matrices.multiply(new Quaternion(new Vec3f(0, 1, 0), 180, true));
                case EAST -> matrices.multiply(new Quaternion(new Vec3f(0, 1, 0), 90, true));
                case SOUTH -> matrices.multiply(new Quaternion(new Vec3f(0, 1, 0), 0, true));
                case WEST -> matrices.multiply(new Quaternion(new Vec3f(0, 1, 0), 270, true));
                default -> {}
            }
            matrices.translate(-.5, -.5, -.5);
        }

        matrices.translate(.25, .75, .25);
        ItemStack stack  = new ItemStack(Items.TOTEM_OF_UNDYING);
        if(stack.getItem() instanceof BlockItem) matrices.scale(.75f, .75f, .75f); // Blocks are scaled to 75%
        else matrices.scale(.375f, .375f, .375f); // Every other item is scaled to 3
        MinecraftClient.getInstance().getItemRenderer().renderItem(
                stack,
                ModelTransformation.Mode.FIXED,
                light, overlay, matrices, vertexConsumers, 0);
        matrices.pop();
    }
}
