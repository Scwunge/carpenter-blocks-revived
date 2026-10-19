package com.carpentersblocks.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.coverable.Overlay;
import com.carpentersblocks.entity.CarpentersTileEntity;

/**
 * The lines Jade and WTHIT add to a Carpenter's block or tile, built once here so both show the same thing. Everything
 * read here is already synced to the client for rendering, so no server data is needed.
 */
public final class TooltipLines {

    private TooltipLines() {}

    /** What the looked-at face shows: its cover, dye, overlay and chisel design, and whether the block is lit. */
    public static List<Component> block(CoverableBlockEntity blockEntity, @Nullable Direction face) {
        CoverData data = blockEntity.getCoverData();
        int slot = face == null ? CoverData.BASE : data.slotFor(face);
        List<Component> lines = new ArrayList<>();
        BlockState cover = data.cover(slot);
        if (cover != null) {
            lines.add(Component.translatable(slot == CoverData.BASE ? "carpentersblocks.tooltip.cover"
                    : "carpentersblocks.tooltip.side_cover", cover.getBlock().getName()));
        }
        DyeColor dye = data.dye(slot);
        if (dye != null) {
            lines.add(Component.translatable("carpentersblocks.tooltip.dye", dyeName(dye)));
        }
        Overlay overlay = data.overlay(slot);
        if (overlay != null) {
            lines.add(Component.translatable("carpentersblocks.tooltip.overlay", overlay.block().getName()));
        }
        String design = data.design(slot);
        if (design != null && !design.isEmpty()) {
            lines.add(Component.translatable("carpentersblocks.tooltip.design", designName(design)));
        }
        if (data.isIlluminated()) {
            lines.add(Component.translatable("carpentersblocks.tooltip.illuminated"));
        }
        return lines;
    }

    /** A tile's design, dye and turn. */
    public static List<Component> tile(CarpentersTileEntity tile) {
        List<Component> lines = new ArrayList<>();
        if (!tile.getDesign().isEmpty()) {
            lines.add(Component.translatable("carpentersblocks.tooltip.design", designName(tile.getDesign())));
        }
        lines.add(Component.translatable("carpentersblocks.tooltip.dye", dyeName(tile.getDye())));
        if (tile.getRotation() != 0) {
            lines.add(Component.translatable("carpentersblocks.tooltip.rotation", tile.getRotation() * 90));
        }
        return lines;
    }

    private static Component dyeName(DyeColor dye) {
        return Component.translatable("color.minecraft." + dye.getName());
    }

    /** Designs have no translations of their own: {@code stock_offset_squares} shows as "Offset Squares". */
    static String designName(String design) {
        String[] words = design.replaceFirst("^stock_", "").split("_");
        StringBuilder name = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (!name.isEmpty()) {
                    name.append(' ');
                }
                name.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
            }
        }
        return name.toString();
    }
}
