package com.carpentersblocks.block.door;

import net.minecraft.util.StringRepresentable;

/** Styles of the Carpenter's Hatch, in the original's hammer order ({@code Hatch.TYPE_*}). */
public enum HatchType implements StringRepresentable {
    HIDDEN("hidden"),
    WINDOW("window"),
    SCREEN("screen"),
    FRENCH_WINDOW("french_window"),
    PANEL("panel");

    private final String name;

    HatchType(String name) {
        this.name = name;
    }

    public HatchType next() {
        return values()[(ordinal() + 1) % values().length];
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
