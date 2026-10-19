package com.carpentersblocks.client.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.config.ConfigOptions;
import com.carpentersblocks.config.ConfigOptions.Option;

/**
 * Edits the config file for the loaders without a built-in config screen (Fabric, through Mod Menu). Every option is
 * read at startup, many of them during registration, so changes are written to the file and apply at the next start.
 * Names come from the same {@code carpentersblocks.configuration.*} strings NeoForge's own config screen uses.
 */
public class ConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int WIDGET_WIDTH = 100;

    private final Screen parent;
    private final Map<Option, Object> values;
    private final Consumer<Map<Option, Object>> save;
    private OptionRows list;

    /**
     * @param values the values in the file now, one per option; edited in place
     * @param save   writes the edited values to the file
     */
    public ConfigScreen(Screen parent, Map<Option, Object> values, Consumer<Map<Option, Object>> save) {
        super(Component.translatable(CarpentersBlocks.MOD_ID + ".configuration.title"));
        this.parent = parent;
        this.values = new LinkedHashMap<>(values);
        this.save = save;
    }

    private static String key(String name) {
        return CarpentersBlocks.MOD_ID + ".configuration." + name;
    }

    @Override
    protected void init() {
        list = addRenderableWidget(new OptionRows(minecraft, width, height - 64, 32));
        for (String section : ConfigOptions.SECTIONS) {
            list.add(new Heading(Component.translatable(key(section)).withStyle(ChatFormatting.BOLD)));
            for (Option option : ConfigOptions.inSection(section)) {
                list.add(new Setting(option, widget(option)));
            }
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
            save.accept(values);
            onClose();
        }).bounds(width / 2 - 154, height - 28, 150, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(width / 2 + 4, height - 28, 150, 20).build());
    }

    private AbstractWidget widget(Option option) {
        AbstractWidget widget;
        if (option.type() == Option.Type.BOOLEAN) {
            widget = CycleButton.onOffBuilder((Boolean) values.get(option)).displayOnlyValue()
                    .create(0, 0, WIDGET_WIDTH, 20, Component.translatable(key(option.key())),
                            (button, value) -> values.put(option, value));
        } else {
            EditBox box = new EditBox(font, 0, 0, WIDGET_WIDTH, 20, Component.translatable(key(option.key())));
            box.setValue(String.valueOf(values.get(option)));
            box.setFilter(text -> text.matches(option.type() == Option.Type.INTEGER ? "\\d*" : "\\d*\\.?\\d*"));
            box.setResponder(text -> {
                try {
                    Number number = option.type() == Option.Type.INTEGER ? Long.parseLong(text)
                            : Double.parseDouble(text);
                    values.put(option, option.clamp(number));
                    box.setTextColor(number.doubleValue() < option.min() || number.doubleValue() > option.max()
                            ? 0xFF5555 : 0xE0E0E0);
                } catch (NumberFormatException e) {
                    box.setTextColor(0xFF5555);
                }
            });
            widget = box;
        }
        widget.setTooltip(Tooltip.create(Component.literal(String.join(" ", option.comment()))));
        return widget;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable(key("restart")), width / 2, 20, 0xA0A0A0);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private static class OptionRows extends ContainerObjectSelectionList<Row> {

        OptionRows(Minecraft minecraft, int width, int height, int y) {
            super(minecraft, width, height, y, ROW_HEIGHT);
        }

        void add(Row row) {
            addEntry(row);
        }

        @Override
        public int getRowWidth() {
            return 320;
        }
    }

    private abstract static class Row extends ContainerObjectSelectionList.Entry<Row> {
    }

    private class Heading extends Row {

        private final Component text;

        Heading(Component text) {
            this.text = text;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX,
                int mouseY, boolean hovering, float partialTick) {
            graphics.drawCenteredString(font, text, left + width / 2, top + (height - font.lineHeight) / 2, 0xFFFFFF);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }

    private class Setting extends Row {

        private final Component name;
        private final AbstractWidget widget;

        Setting(Option option, AbstractWidget widget) {
            this.name = Component.translatable(key(option.key()));
            this.widget = widget;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX,
                int mouseY, boolean hovering, float partialTick) {
            graphics.drawString(font, name, left, top + (height - font.lineHeight) / 2, 0xFFFFFF);
            widget.setPosition(left + width - WIDGET_WIDTH, top);
            widget.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return new ArrayList<>(List.of(widget));
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(widget);
        }
    }
}
