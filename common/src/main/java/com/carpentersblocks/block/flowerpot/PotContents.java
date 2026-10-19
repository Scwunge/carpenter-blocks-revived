package com.carpentersblocks.block.flowerpot;

import net.minecraft.util.StringRepresentable;

/** What the pot holds, for its shape: nothing (or just soil), a plant, or a plant you can't walk through. */
public enum PotContents implements StringRepresentable {
    EMPTY("empty"),
    PLANT("plant"),
    SOLID("solid");

    private final String name;

    PotContents(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
