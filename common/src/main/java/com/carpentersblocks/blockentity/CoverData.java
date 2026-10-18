package com.carpentersblocks.blockentity;

import java.util.Arrays;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * Immutable snapshot of a block's covers, safe to hand to chunk-building threads.
 * <p>
 * Slots 0-5 are side covers indexed by {@link Direction#get3DDataValue()}, slot 6 is the base cover, matching the
 * attribute layout of the original mod.
 */
public final class CoverData {

    public static final int BASE = 6;
    public static final int SLOTS = 7;
    public static final CoverData EMPTY = new CoverData(new BlockState[SLOTS], false);

    private final BlockState[] covers;
    private final boolean illuminated;

    private CoverData(BlockState[] covers, boolean illuminated) {
        this.covers = covers;
        this.illuminated = illuminated;
    }

    @Nullable
    public BlockState cover(int slot) {
        return covers[slot];
    }

    public boolean hasCover(int slot) {
        return covers[slot] != null;
    }

    /** Slot whose cover is shown on the given face: the side cover if present, otherwise the base. */
    public int slotFor(Direction face) {
        return hasCover(face.get3DDataValue()) ? face.get3DDataValue() : BASE;
    }

    @Nullable
    public BlockState coverFor(Direction face) {
        return covers[slotFor(face)];
    }

    public boolean isIlluminated() {
        return illuminated;
    }

    public boolean isEmpty() {
        return !illuminated && Arrays.stream(covers).allMatch(c -> c == null);
    }

    public CoverData withCover(int slot, @Nullable BlockState state) {
        BlockState[] copy = covers.clone();
        copy[slot] = state;
        return new CoverData(copy, illuminated);
    }

    public CoverData withIlluminated(boolean value) {
        return new CoverData(covers, value);
    }
}
