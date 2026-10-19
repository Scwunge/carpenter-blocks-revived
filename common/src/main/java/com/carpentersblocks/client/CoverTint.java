package com.carpentersblocks.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.coverable.Overlay;

/**
 * Colors the re-textured quads of a coverable block.
 * <p>
 * Each quad's tint index packs what kind of color it wants (the cover's own tint, a dye, or an overlay's tint), the
 * attribute slot it came from, and for covers the cover's own tint index, so the color can be looked up here.
 */
public final class CoverTint implements BlockColor {

    public static final CoverTint INSTANCE = new CoverTint();

    private static final int KIND_COVER = 0;
    private static final int KIND_DYE = 1;
    private static final int KIND_OVERLAY = 2;
    private static final int KIND_CUSTOM = 3;

    private static final int SLOT_SHIFT = 8;
    private static final int KIND_SHIFT = 12;
    private static final int TINT_MASK = (1 << SLOT_SHIFT) - 1;
    private static final int SLOT_MASK = (1 << (KIND_SHIFT - SLOT_SHIFT)) - 1;

    /** Colors of {@link #custom} quads, by block. */
    private static final Map<Block, BlockColor> CUSTOM = new ConcurrentHashMap<>();

    private CoverTint() {}

    /** The cover's own tinting (grass, leaves, water) for one of its tint indexes. */
    public static int cover(int slot, int coverTintIndex) {
        return encode(KIND_COVER, slot, coverTintIndex);
    }

    /** The dye color of a slot, which replaces the cover's tint. */
    public static int dye(int slot) {
        return encode(KIND_DYE, slot, 0);
    }

    /** The tint of a slot's overlay block (grass and vines). */
    public static int overlay(int slot) {
        return encode(KIND_OVERLAY, slot, 0);
    }

    /**
     * A color the block works out itself (the soil and plant in a flower pot), from the color registered for it with
     * {@link #registerCustom}, which gets {@code tint} (0 to 255) as its tint index.
     */
    public static int custom(int tint) {
        return encode(KIND_CUSTOM, CoverData.BASE, tint);
    }

    public static void registerCustom(Block block, BlockColor color) {
        CUSTOM.put(block, color);
    }

    private static int encode(int kind, int slot, int tint) {
        return (kind << KIND_SHIFT) | ((slot + 1) << SLOT_SHIFT) | (tint & TINT_MASK);
    }

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        int slot = ((tintIndex >> SLOT_SHIFT) & SLOT_MASK) - 1;
        if (slot < 0 || slot >= CoverData.SLOTS || level == null || pos == null
                || !(level.getBlockEntity(pos) instanceof CoverableBlockEntity blockEntity)) {
            return -1;
        }
        CoverData data = blockEntity.getCoverData();

        switch (tintIndex >> KIND_SHIFT) {
            case KIND_DYE -> {
                DyeColor dye = data.dye(slot);
                return dye == null ? -1 : dye.getFireworkColor();
            }
            case KIND_CUSTOM -> {
                BlockColor custom = CUSTOM.get(state.getBlock());
                return custom == null ? -1 : custom.getColor(state, level, pos, tintIndex & TINT_MASK);
            }
            case KIND_OVERLAY -> {
                Overlay overlay = data.overlay(slot);
                return overlay == null ? -1
                        : Minecraft.getInstance().getBlockColors()
                                .getColor(overlay.block().defaultBlockState(), level, pos, 0);
            }
            default -> {
                BlockState cover = data.cover(slot);
                return cover == null ? -1
                        : Minecraft.getInstance().getBlockColors()
                                .getColor(cover, level, pos, tintIndex & TINT_MASK);
            }
        }
    }
}
