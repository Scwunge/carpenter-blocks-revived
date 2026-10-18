package com.carpentersblocks.block.ladder;

import net.minecraft.util.StringRepresentable;

/** The Carpenter's Ladder styles, in the original's order; the hammer steps through them. */
public enum LadderType implements StringRepresentable {
    /** Two stiles and four rungs, joining neighbors in a row into one wide ladder. */
    DEFAULT("default"),
    /** Iron rails with rungs between. */
    RAIL("rail"),
    /** A pole with alternating pegs, or pegs on a wall. */
    POLE("pole");

    private final String name;

    LadderType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public LadderType next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public LadderType previous() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }
}
