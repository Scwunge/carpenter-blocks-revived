package com.carpentersblocks.block.coverable;

import java.util.Arrays;
import java.util.Objects;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * Immutable snapshot of everything applied to a coverable block, safe to hand to chunk-building threads.
 * <p>
 * Slots 0-5 are the sides, indexed by {@link Direction#get3DDataValue()}, and slot 6 is the base, matching the
 * attribute layout of the original {@code TEBase}. Each slot can hold a cover, a dye, an overlay and a chisel design.
 */
public final class CoverData {

    public static final int BASE = 6;
    public static final int SLOTS = 7;
    public static final CoverData EMPTY = new CoverData(new BlockState[SLOTS], new DyeColor[SLOTS],
            new Overlay[SLOTS], new String[SLOTS], false);

    private final BlockState[] covers;
    private final DyeColor[] dyes;
    private final Overlay[] overlays;
    private final String[] designs;
    private final boolean illuminated;

    private CoverData(BlockState[] covers, DyeColor[] dyes, Overlay[] overlays, String[] designs,
            boolean illuminated) {
        this.covers = covers;
        this.dyes = dyes;
        this.overlays = overlays;
        this.designs = designs;
        this.illuminated = illuminated;
    }

    @Nullable
    public BlockState cover(int slot) {
        return covers[slot];
    }

    public boolean hasCover(int slot) {
        return covers[slot] != null;
    }

    @Nullable
    public DyeColor dye(int slot) {
        return dyes[slot];
    }

    @Nullable
    public Overlay overlay(int slot) {
        return overlays[slot];
    }

    @Nullable
    public String design(int slot) {
        return designs[slot];
    }

    public boolean isIlluminated() {
        return illuminated;
    }

    /** Slot whose attributes show on the given face: the side's if it has a cover, otherwise the base. */
    public int slotFor(Direction face) {
        return hasCover(face.get3DDataValue()) ? face.get3DDataValue() : BASE;
    }

    @Nullable
    public BlockState coverFor(Direction face) {
        return covers[slotFor(face)];
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public CoverData withCover(int slot, @Nullable BlockState state) {
        BlockState[] copy = covers.clone();
        copy[slot] = state;
        return new CoverData(copy, dyes, overlays, designs, illuminated);
    }

    public CoverData withDye(int slot, @Nullable DyeColor dye) {
        DyeColor[] copy = dyes.clone();
        copy[slot] = dye;
        return new CoverData(covers, copy, overlays, designs, illuminated);
    }

    public CoverData withOverlay(int slot, @Nullable Overlay overlay) {
        Overlay[] copy = overlays.clone();
        copy[slot] = overlay;
        return new CoverData(covers, dyes, copy, designs, illuminated);
    }

    public CoverData withDesign(int slot, @Nullable String design) {
        String[] copy = designs.clone();
        copy[slot] = design;
        return new CoverData(covers, dyes, overlays, copy, illuminated);
    }

    public CoverData withIlluminated(boolean value) {
        return new CoverData(covers, dyes, overlays, designs, value);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CoverData other && illuminated == other.illuminated
                && Arrays.equals(covers, other.covers) && Arrays.equals(dyes, other.dyes)
                && Arrays.equals(overlays, other.overlays) && Arrays.equals(designs, other.designs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(covers), Arrays.hashCode(dyes), Arrays.hashCode(overlays),
                Arrays.hashCode(designs), illuminated);
    }
}
