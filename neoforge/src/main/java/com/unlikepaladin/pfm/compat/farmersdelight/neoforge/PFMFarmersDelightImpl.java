package com.unlikepaladin.pfm.compat.farmersdelight.neoforge;

import com.unlikepaladin.pfm.blocks.IronStoveBlock;
import com.unlikepaladin.pfm.blocks.StoveBlock;
import com.unlikepaladin.pfm.compat.farmersdelight.PFMFarmersDelight;
import com.unlikepaladin.pfm.data.FurnitureBlock;
import com.unlikepaladin.pfm.registry.PaladinFurnitureModBlocksItems;
import com.unlikepaladin.pfm.runtime.data.PFMTagProvider;
import net.minecraft.block.Block;
import vectorwing.farmersdelight.FarmersDelight;

import java.util.ArrayList;
import java.util.List;

public class PFMFarmersDelightImpl extends PFMFarmersDelight {
    public static PFMFarmersDelight getInstance() {
        return new PFMFarmersDelightImpl();
    }

    @Override
    public void generateTags() {
        List<Block> stoves = new ArrayList<>(StoveBlock.streamStoves().map(FurnitureBlock::getBlock).toList());
        stoves.addAll(IronStoveBlock.streamIronStoves().map(FurnitureBlock::getBlock).toList());
        stoves.add(PaladinFurnitureModBlocksItems.KITCHEN_STOVETOP);
        PFMTagProvider.getOrCreateTagBuilder(HEAT_SOURCES)
                .add(stoves.toArray(new Block[0]));
    }

    @Override
    public String getModId() {
        return FarmersDelight.MODID;
    }
}
