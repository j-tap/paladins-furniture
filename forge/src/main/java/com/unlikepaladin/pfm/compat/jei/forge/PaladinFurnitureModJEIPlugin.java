package com.unlikepaladin.pfm.compat.jei.forge;

import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.compat.jei.FreezingCategory;
import com.unlikepaladin.pfm.compat.jei.FurnitureCategory;
import com.unlikepaladin.pfm.compat.jei.PaladinFurnitureModJEI;
import com.unlikepaladin.pfm.recipes.FreezingRecipe;
import com.unlikepaladin.pfm.recipes.FurnitureRecipe;
import com.unlikepaladin.pfm.registry.PaladinFurnitureModBlocksItems;
import com.unlikepaladin.pfm.registry.RecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@JeiPlugin
public class PaladinFurnitureModJEIPlugin implements IModPlugin {
    public void PaladinFurnitureModJEIPlugin(){

    }
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = Objects.requireNonNull(mc.world);

        List<FreezingRecipe> freezingRecipes = world.getRecipeManager().listAllOfType(RecipeTypes.FREEZING_RECIPE);
        registration.addRecipes(PaladinFurnitureModJEI.FREEZING_RECIPE, freezingRecipes);

        List<FurnitureRecipe> furnitureRecipes = world.getRecipeManager().listAllOfType(RecipeTypes.FURNITURE_RECIPE);
        registration.addRecipes(PaladinFurnitureModJEI.FURNITURE_RECIPE, furnitureRecipes);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new FurnitureCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FreezingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(new ItemStack(PaladinFurnitureModBlocksItems.WHITE_FREEZER), PaladinFurnitureModJEI.FREEZING_RECIPE);
        registration.addRecipeCatalyst(new ItemStack(PaladinFurnitureModBlocksItems.IRON_FREEZER), PaladinFurnitureModJEI.FREEZING_RECIPE);
        registration.addRecipeCatalyst(new ItemStack(PaladinFurnitureModBlocksItems.GRAY_FREEZER), PaladinFurnitureModJEI.FREEZING_RECIPE);
        registration.addRecipeCatalyst(new ItemStack(PaladinFurnitureModBlocksItems.WORKING_TABLE), PaladinFurnitureModJEI.FURNITURE_RECIPE);
    }

    @Override
    public Identifier getPluginUid() {
        return new Identifier(PaladinFurnitureMod.MOD_ID, "jei_plugin");
    }
}