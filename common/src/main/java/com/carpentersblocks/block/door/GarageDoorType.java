package com.carpentersblocks.block.door;

import net.minecraft.util.StringRepresentable;

/** Styles of the Carpenter's Garage Door, in the original's hammer order ({@code GarageDoor.TYPE_*}). */
public enum GarageDoorType implements StringRepresentable {
    DEFAULT("default"),
    GLASS_TOP("glass_top"),
    GLASS("glass"),
    SIDING("siding"),
    HIDDEN("hidden");

    private final String name;

    GarageDoorType(String name) {
        this.name = name;
    }

    public GarageDoorType next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public GarageDoorType previous() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
