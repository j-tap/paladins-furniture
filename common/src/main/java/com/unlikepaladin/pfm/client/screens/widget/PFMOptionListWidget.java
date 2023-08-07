package com.unlikepaladin.pfm.client.screens.widget;

import com.google.common.collect.ImmutableList;
import com.unlikepaladin.pfm.PaladinFurnitureMod;
import com.unlikepaladin.pfm.client.screens.PFMConfigScreen;
import com.unlikepaladin.pfm.config.option.AbstractConfigOption;
import com.unlikepaladin.pfm.config.option.BooleanConfigOption;
import com.unlikepaladin.pfm.config.option.Side;
import com.unlikepaladin.pfm.runtime.PFMDataGen;
import com.unlikepaladin.pfm.runtime.PFMRuntimeResources;
import com.unlikepaladin.pfm.utilities.PFMFileUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PFMOptionListWidget extends ElementListWidget<PFMOptionListWidget.Entry> {
    final PFMConfigScreen parent;
    int maxKeyNameLength;

    public PFMOptionListWidget(PFMConfigScreen parent, MinecraftClient client) {
        super(client, parent.width + 125, parent.height, 43, parent.height - 32, 20);
        this.parent = parent;
        String string = null;
        for(Map.Entry<String, AbstractConfigOption> configOptionEntry : PaladinFurnitureMod.getPFMConfig().options.entrySet()) {
            Text text;
            int i;
            String configOptionCategory = configOptionEntry.getValue().getCategory();
            if (!configOptionCategory.equals(string)) {
                string = configOptionCategory;
                this.addEntry(new CategoryEntry(Text.translatable(configOptionCategory)));
            }
            if ((i = client.textRenderer.getWidth(text = configOptionEntry.getValue().getTitle())) > this.maxKeyNameLength) {
                this.maxKeyNameLength = i;
            }
            if (configOptionEntry.getValue().getType() == Boolean.class) {
                this.addEntry(new BooleanEntry((BooleanConfigOption)configOptionEntry.getValue(), text));
            } else {
                PaladinFurnitureMod.GENERAL_LOGGER.warn("Unsupported Config Type!");
            }
        }
        this.addEntry(new CategoryEntry(Text.literal("")));
        this.addEntry(new ButtonEntry(Side.CLIENT, Text.translatable("pfm.option.regenAssets"), Text.translatable("pfm.config.regen"), Text.translatable("pfm.option.regenAssets.tooltip"), button -> {
            PFMFileUtil.deleteDir(PFMRuntimeResources.getResourceDirectory().toFile());
            PFMDataGen.frozen = false;
            PFMRuntimeResources.prepareAsyncResourceGen(true);
            PFMRuntimeResources.runAsyncResourceGen();
            MinecraftClient.getInstance().reloadResourcesConcurrently();
        }));
    }

    @Override
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 15;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    @Environment(value= EnvType.CLIENT)
    public class CategoryEntry
            extends Entry {
        final MutableText text;
        private final int textWidth;

        public CategoryEntry(MutableText text) {
            this.text = text;
            this.textWidth = PFMOptionListWidget.this.client.textRenderer.getWidth(this.text);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(PFMOptionListWidget.this.client.textRenderer, this.text.setStyle(Style.EMPTY.withBold(true)), (PFMOptionListWidget.this.client.currentScreen.width / 2 - this.textWidth / 2), y + entryHeight - (PFMOptionListWidget.this).client.textRenderer.fontHeight - 1, 0xFFFFFF, true);
        }

        public boolean changeFocus(boolean lookForwards) {
            return false;
        }

        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(new Selectable(){

                @Override
                public Selectable.SelectionType getType() {
                    return Selectable.SelectionType.HOVERED;
                }

                @Override
                public void appendNarrations(NarrationMessageBuilder builder) {
                    builder.put(NarrationPart.TITLE, PFMOptionListWidget.CategoryEntry.this.text);
                }
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class BooleanEntry
            extends Entry {
        private final BooleanConfigOption configOption;
        private final Text optionName;
        private final ButtonWidget valueButton;
        private final ButtonWidget resetButton;

        private final Supplier<MutableText> supplier;

        BooleanEntry(final BooleanConfigOption configOption, final Text optionName) {
            this.configOption = configOption;
            this.optionName = optionName;
            this.supplier = () -> {
                final MutableText sideText = configOption.getSide() == Side.CLIENT ? Text.translatable("pfm.option.client").setStyle(Style.EMPTY.withItalic(false).withBold(true).withColor(0xf77f34)) : Text.translatable("pfm.option.server").setStyle((Style.EMPTY.withItalic(false).withBold(true).withColor(0xf77f34)));
                final MutableText styledTooltip = ((MutableText)configOption.getToolTip()).setStyle(Style.EMPTY.withItalic(true));
                return sideText.append(Text.literal("\n")).append(styledTooltip);
            };
            this.valueButton = ButtonWidget.builder(optionName, button -> {
                PFMOptionListWidget.this.parent.focusedConfigOption = configOption;
                configOption.setValue(!configOption.getValue());
            }).dimensions(0,0,75,20).narrationSupplier(textSupplier -> this.supplier.get()).build();

            this.resetButton = ButtonWidget.builder(Text.translatable("controls.reset"), button -> {
                configOption.setValue(configOption.getDefaultValue());
            }).dimensions(0,0,50,20).narrationSupplier(textSupplier -> Text.translatable("narrator.controls.reset", optionName)).build();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(PFMOptionListWidget.this.client.textRenderer, this.optionName, (x + 90 - PFMOptionListWidget.this.maxKeyNameLength), (y + entryHeight / 2 - PFMOptionListWidget.this.client.textRenderer.fontHeight / 2), 0xFFFFFF, false);
            this.resetButton.setX(x + 190);
            this.resetButton.setY(y);
            this.resetButton.active = this.configOption.getSide() == Side.SERVER ? !PFMConfigScreen.isOnServer && !this.configOption.isDefault() : !this.configOption.isDefault();;
            this.resetButton.render(context, mouseX, mouseY, tickDelta);
            this.valueButton.setX(x + 105);
            this.valueButton.setY(y);
            this.valueButton.setMessage(this.configOption.getValue() ? ScreenTexts.YES : ScreenTexts.NO);
            this.valueButton.active = this.configOption.getSide() != Side.SERVER || !PFMConfigScreen.isOnServer;
            this.valueButton.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public List<? extends Element> children() {
            return ImmutableList.of(this.valueButton, this.resetButton);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(this.valueButton, this.resetButton);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.valueButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            return this.resetButton.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.valueButton.mouseReleased(mouseX, mouseY, button) || this.resetButton.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class ButtonEntry
            extends Entry {
        private final Text optionName;
        private final ButtonWidget button;

        private final Supplier<MutableText> supplier;
        private final Side side;
        ButtonEntry(Side side, final Text optionName, Text buttonText, Text tooltip, ButtonWidget.PressAction pressAction) {
            this.optionName = optionName;
            this.side = side;
            this.supplier = () -> {
                final MutableText sideText = side == Side.CLIENT ? Text.translatable("pfm.option.client").setStyle(Style.EMPTY.withItalic(false).withBold(true).withColor(0xf77f34)) : Text.translatable("pfm.option.server").setStyle((Style.EMPTY.withItalic(false).withBold(true).withColor(0xf77f34)));
                final MutableText styledTooltip = ((MutableText)tooltip).setStyle(Style.EMPTY.withItalic(true));
                return sideText.append(Text.literal("\n")).append(styledTooltip);
            };

            this.button = new ButtonWidget(0, 0, 135, 20, buttonText, pressAction, textSupplier -> supplier.get()){
                @Override
                protected MutableText getNarrationMessage() {
                    return (MutableText) optionName;
                }
            };
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(PFMOptionListWidget.this.client.textRenderer, this.optionName, (x + 90 - PFMOptionListWidget.this.maxKeyNameLength), (y + entryHeight / 2 - PFMOptionListWidget.this.client.textRenderer.fontHeight / 2), 0xFFFFFF, false);
            this.button.setX(x+105);
            this.button.setY(y);
            this.button.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public List<? extends Element> children() {
            return ImmutableList.of(this.button);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(this.button);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.button.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.button.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry extends ElementListWidget.Entry<Entry> {
    }
}

