package com.unlikepaladin.pfm.compat.rei.fabric;

import com.google.common.collect.Lists;
import com.unlikepaladin.pfm.registry.PaladinFurnitureModBlocksItems;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.TransferRecipeCategory;
import me.shedaniel.rei.api.widgets.Slot;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingDisplay;
import me.shedaniel.rei.server.ContainerInfo;
import me.shedaniel.rei.server.ContainerInfoHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FurnitureCategory implements TransferRecipeCategory<FurnitureDisplay> {
    public static final EntryStack ICON = EntryStack.create(PaladinFurnitureModBlocksItems.WORKING_TABLE);

    public static int getSlotWithSize(FurnitureDisplay recipeDisplay, int num, int craftingGridWidth) {
        int x = num % recipeDisplay.getWidth();
        int y = (num - x) / recipeDisplay.getWidth();
        return craftingGridWidth * y + x;
    }

    @Override
    public @NotNull Identifier getIdentifier() {
        return FurnitureDisplay.IDENTIFIER;
    }


    @Override
    public @NotNull EntryStack getLogo() {
        return ICON;
    }

    @Override
    public @NotNull String getCategoryName() {
        return I18n.translate("rei.pfm.furniture");
    }

    @Override
    public @NotNull List<Widget> setupDisplay(FurnitureDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 60, startPoint.y + 18)));
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 95, startPoint.y + 19)));
        List<List<EntryStack>> input = display.getInputEntries();
        List<Slot> slots = Lists.newArrayList();

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                slots.add(Widgets.createSlot(new Point(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18)).markInput());
            }
        }
        for (int z = 0; z < input.size(); z++)
            slots.get(z).entries(input.get(z));

        widgets.addAll(slots);
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 95, startPoint.y + 19)).entries(display.getResultingEntries().get(0)).disableBackground().markOutput());
        return widgets;
    }

    @Override
    public void renderRedSlots(MatrixStack matrices, List<Widget> widgets, Rectangle bounds, FurnitureDisplay display, IntList redSlots) {
        if (REIHelper.getInstance().getPreviousContainerScreen() == null) return;
        ContainerInfo<ScreenHandler> info = (ContainerInfo<ScreenHandler>) ContainerInfoHandler.getContainerInfo(getIdentifier(), REIHelper.getInstance().getPreviousContainerScreen().getScreenHandler().getClass());
        if (info == null)
            return;
        matrices.push();
        matrices.translate(0, 0, 400);
        Point startPoint = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 27);
        int width = info.getCraftingWidth(REIHelper.getInstance().getPreviousContainerScreen().getScreenHandler());
        for (Integer slot : redSlots) {
            int i = slot;
            int x = i % width;
            int y = MathHelper.floor(i / (float) width);
            DrawableHelper.fill(matrices, startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18, startPoint.x + 1 + x * 18 + 16, startPoint.y + 1 + y * 18 + 16, 0x60ff0000);
        }
        matrices.pop();
    }
}