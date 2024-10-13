package com.unlikepaladin.pfm.blocks.models.mirror.neoforge;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.texture.Sprite;

import java.util.List;
import java.util.Map;

public class UnbakedMirrorModelImpl {
    public static BakedModel getBakedModel(Sprite frame, Sprite glassTex, Sprite reflectTex, ModelBakeSettings settings, Map<String,BakedModel> bakedModels, List<String> MODEL_PARTS) {
        return new NeoForgeMirrorModel(frame, glassTex, reflectTex, settings, bakedModels, MODEL_PARTS);
    }
}