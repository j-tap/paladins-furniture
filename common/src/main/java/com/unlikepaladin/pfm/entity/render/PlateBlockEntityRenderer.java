package com.unlikepaladin.pfm.entity.render;

import com.unlikepaladin.pfm.blocks.PlateBlock;
import com.unlikepaladin.pfm.blocks.blockentities.PlateBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;

public class PlateBlockEntityRenderer<T extends PlateBlockEntity> extends BlockEntityRenderer<T> {
    public ItemStack itemStack;
    private static final float SCALE = 0.375f;
    public PlateBlockEntityRenderer(BlockEntityRenderDispatcher ctx) {
        super(ctx);
    }
    @Override
    public void render(PlateBlockEntity plateBlockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
        Direction direction = plateBlockEntity.getCachedState().get(PlateBlock.FACING);
        itemStack = plateBlockEntity.getItemInPlate();
        matrices.push();
        Direction direction2 = Direction.fromHorizontal((direction.getHorizontal()) % 4);
        float g = -direction2.asRotation();
        Direction dir = plateBlockEntity.getCachedState().get(PlateBlock.FACING);
        if (dir == Direction.NORTH) {
            matrices.translate(0.5, 0.08, 0.65);
        }
        else if (dir == Direction.SOUTH) {
            matrices.translate(0.5, 0.08, 0.35);
        }
        else if (dir == Direction.WEST) {
            matrices.translate(0.65, 0.08, 0.5);
        }
        else {
            matrices.translate(0.35, 0.08, 0.5);
        }

        int rot = 90;
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(g));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(rot));
        if (Registry.ITEM.getId(itemStack.getItem()).toString().equals("sandwichable:sandwich")) {
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(270.0f));
            matrices.translate(0.0, 0.11, 0.05);
        }
        int lightAbove = WorldRenderer.getLightmapCoordinates(plateBlockEntity.getWorld(), plateBlockEntity.getPos().up());
        MinecraftClient.getInstance().getItemRenderer().renderItem(itemStack, ModelTransformation.Mode.GROUND, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumerProvider);
        matrices.pop();
    }
}
