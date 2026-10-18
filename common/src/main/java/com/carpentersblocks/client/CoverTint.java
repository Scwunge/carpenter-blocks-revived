package com.carpentersblocks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.blockentity.CarpentersBlockEntity;

/**
 * Passes tinting (grass, foliage, water) through to the cover block.
 * <p>
 * Re-textured quads carry a tint index that encodes which cover slot they came from along with the cover's own tint
 * index, so the color can be looked up from the right cover.
 */
public final class CoverTint implements BlockColor {

    public static final CoverTint INSTANCE = new CoverTint();

    private static final int SLOT_SHIFT = 8;
    private static final int TINT_MASK = (1 << SLOT_SHIFT) - 1;

    private CoverTint() {}

    public static int encode(int slot, int coverTintIndex) {
        return ((slot + 1) << SLOT_SHIFT) | (coverTintIndex & TINT_MASK);
    }

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        int slot = (tintIndex >> SLOT_SHIFT) - 1;
        if (slot < 0 || level == null || pos == null) {
            return -1;
        }
        if (!(level.getBlockEntity(pos) instanceof CarpentersBlockEntity blockEntity)) {
            return -1;
        }
        BlockState cover = blockEntity.getCoverData().cover(slot);
        if (cover == null) {
            return -1;
        }
        return Minecraft.getInstance().getBlockColors().getColor(cover, level, pos, tintIndex & TINT_MASK);
    }
}
