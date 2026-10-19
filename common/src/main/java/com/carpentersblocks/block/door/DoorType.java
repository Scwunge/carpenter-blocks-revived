package com.carpentersblocks.block.door;

import net.minecraft.util.StringRepresentable;

/** Styles of the Carpenter's Door, in the original's hammer order ({@code Hinge.TYPE_*}). */
public enum DoorType implements StringRepresentable {
    GLASS_TOP("glass_top"),
    GLASS_TALL("glass_tall"),
    PANELS("panels"),
    SCREEN_TALL("screen_tall"),
    FRENCH_GLASS("french_glass"),
    HIDDEN("hidden");

    private final String name;

    DoorType(String name) {
        this.name = name;
    }

    public DoorType next() {
        return values()[(ordinal() + 1) % values().length];
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
