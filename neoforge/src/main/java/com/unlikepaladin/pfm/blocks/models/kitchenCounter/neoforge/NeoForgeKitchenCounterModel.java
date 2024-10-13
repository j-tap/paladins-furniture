package com.unlikepaladin.pfm.blocks.models.kitchenCounter.neoforge;

import com.unlikepaladin.pfm.blocks.KitchenCounterBlock;
import com.unlikepaladin.pfm.blocks.models.ModelHelper;
import com.unlikepaladin.pfm.blocks.models.neoforge.ModelBitSetProperty;
import com.unlikepaladin.pfm.blocks.models.neoforge.PFMNeoForgeBakedModel;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import net.minecraft.util.math.random.Random;

public class NeoForgeKitchenCounterModel extends PFMNeoForgeBakedModel {
    public NeoForgeKitchenCounterModel(ModelBakeSettings settings, List<BakedModel> modelParts) {
        super(settings, modelParts);
    }
    public static ModelProperty<ModelBitSetProperty> CONNECTIONS = new ModelProperty<>();
    public static ModelProperty<BlockState> NEIGHBOR_FACING = new ModelProperty<>();
    public static ModelProperty<BlockState> NEIGHBOR_OPPOSITE = new ModelProperty<>();

    @NotNull
    @Override
    public ModelData getModelData(@NotNull BlockRenderView world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData tileData) {
        if (state.getBlock() instanceof KitchenCounterBlock) {
            ModelData.Builder builder = ModelData.builder();

            ModelData data = builder.build();
            data = super.getModelData(world, pos, state, data);

            KitchenCounterBlock block = (KitchenCounterBlock) state.getBlock();
            Direction direction = state.get(KitchenCounterBlock.FACING);
            boolean right = block.canConnect(world, pos, direction.rotateYCounterclockwise());
            boolean left = block.canConnect(world, pos, direction.rotateYClockwise());
            BlockState neighborStateFacing = world.getBlockState(pos.offset(direction));
            BlockState neighborStateOpposite = world.getBlockState(pos.offset(direction.getOpposite()));
            boolean isNeighborStateOppositeFacingDifferentDirection;
            if (neighborStateOpposite.contains(Properties.HORIZONTAL_FACING)) {
                Direction direction3;
                if (neighborStateOpposite.getBlock() instanceof AbstractFurnaceBlock) {
                    direction3 = neighborStateOpposite.get(Properties.HORIZONTAL_FACING).getOpposite();
                }
                else {
                    direction3 = neighborStateOpposite.get(Properties.HORIZONTAL_FACING);
                }
                isNeighborStateOppositeFacingDifferentDirection = block.isDifferentOrientation(state, world, pos, direction3);
            } else {
                isNeighborStateOppositeFacingDifferentDirection = false;
            }

            boolean isNeighborStateFacingDifferentDirection;
            if (neighborStateFacing.contains(Properties.HORIZONTAL_FACING)) {
                Direction direction2 = neighborStateFacing.get(Properties.HORIZONTAL_FACING);
                isNeighborStateFacingDifferentDirection = block.isDifferentOrientation(state, world, pos, direction2.getOpposite());
            } else {
                isNeighborStateFacingDifferentDirection = false;
            }
            BitSet set = new BitSet();
            set.set(0, left);
            set.set(1, right);
            set.set(2, isNeighborStateOppositeFacingDifferentDirection);
            set.set(3, isNeighborStateFacingDifferentDirection);
            data = data.derive().with(CONNECTIONS, new ModelBitSetProperty(set)).with(NEIGHBOR_FACING, neighborStateFacing).with(NEIGHBOR_OPPOSITE, neighborStateOpposite).build();
            return data;
        }
        return tileData;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull ModelData extraData, RenderLayer renderLayer) {
        if (state != null && state.getBlock() instanceof KitchenCounterBlock && extraData.get(CONNECTIONS) != null && extraData.get(CONNECTIONS).connections != null) {
            BitSet set = extraData.get(CONNECTIONS).connections;
            Direction direction = state.get(KitchenCounterBlock.FACING);
            KitchenCounterBlock block = (KitchenCounterBlock) state.getBlock();
            boolean left = set.get(0);
            boolean right = set.get(1);
            boolean isNeighborStateOppositeFacingDifferentDirection =  set.get(2);
            boolean isNeighborStateFacingDifferentDirection = set.get(3);
            BlockState neighborStateFacing = extraData.get(NEIGHBOR_FACING);
            BlockState neighborStateOpposite = extraData.get(NEIGHBOR_OPPOSITE);
            List<Sprite> spriteList = getSpriteList(state);
            if (block.canConnectToCounter(neighborStateFacing) && neighborStateFacing.contains(Properties.HORIZONTAL_FACING)) {
                Direction direction2 = neighborStateFacing.get(Properties.HORIZONTAL_FACING);
                if (direction2.getAxis() != state.get(Properties.HORIZONTAL_FACING).getAxis() && isNeighborStateFacingDifferentDirection) {
                    if (direction2 == direction.rotateYCounterclockwise()) {
                        return getQuadsWithTexture(getTemplateBakedModels().get(5).getQuads(state, side, rand, extraData, renderLayer), ModelHelper.getOakPlankLogSprites(), spriteList);
                    }
                    else {
                        return getQuadsWithTexture(getTemplateBakedModels().get(6).getQuads(state, side, rand, extraData, renderLayer), ModelHelper.getOakPlankLogSprites(), spriteList);
                    }
                } else {
                    return getQuadsWithTexture(getMiddleQuads(state, side, rand, extraData, renderLayer, left, right), ModelHelper.getOakPlankLogSprites(), spriteList);
                }
            }
            else if (block.canConnectToCounter(neighborStateOpposite) && neighborStateOpposite.contains(Properties.HORIZONTAL_FACING)) {
                Direction direction3;
                if (neighborStateOpposite.getBlock() instanceof AbstractFurnaceBlock) {
                    direction3 = neighborStateOpposite.get(Properties.HORIZONTAL_FACING).getOpposite();
                }
                else {
                    direction3 = neighborStateOpposite.get(Properties.HORIZONTAL_FACING);
                }
                if (direction3.getAxis() != state.get(Properties.HORIZONTAL_FACING).getAxis() && isNeighborStateOppositeFacingDifferentDirection) {
                    if (direction3 == direction.rotateYCounterclockwise()) {
                        return getQuadsWithTexture(getTemplateBakedModels().get(4).getQuads(state, side, rand, extraData, renderLayer), ModelHelper.getOakPlankLogSprites(), spriteList);
                    } else {
                        return getQuadsWithTexture(getTemplateBakedModels().get(3).getQuads(state, side, rand, extraData, renderLayer), ModelHelper.getOakPlankLogSprites(), spriteList);
                    }
                } else {
                    return getQuadsWithTexture(getMiddleQuads(state, side, rand, extraData, renderLayer, left, right), ModelHelper.getOakPlankLogSprites(), spriteList);
                }
            }
            else {
                return getQuadsWithTexture(getMiddleQuads(state, side, rand, extraData, renderLayer, left, right), ModelHelper.getOakPlankLogSprites(), spriteList);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<BakedQuad> getQuads(ItemStack stack, @Nullable BlockState state, @Nullable Direction face, Random random) {
        List<Sprite> spriteList = getSpriteList(state);
        return getQuadsWithTexture(getTemplateBakedModels().get(0).getQuads(state, face, random), ModelHelper.getOakPlankLogSprites(), spriteList);
    }

    private List<BakedQuad> getMiddleQuads(BlockState state, Direction side, Random rand, ModelData extraData, RenderLayer renderLayer, boolean left, boolean right) {
        if (left && right) {
            return getTemplateBakedModels().get(0).getQuads(state, side, rand, extraData, renderLayer);
        } else if (left) {
            return getTemplateBakedModels().get(1).getQuads(state, side, rand, extraData, renderLayer);
        } else if (right) {
            return getTemplateBakedModels().get(2).getQuads(state, side, rand, extraData, renderLayer);
        } else {
            return getTemplateBakedModels().get(0).getQuads(state, side, rand, extraData, renderLayer);
        }
    }
}