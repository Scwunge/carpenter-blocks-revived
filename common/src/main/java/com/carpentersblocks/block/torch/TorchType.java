package com.carpentersblocks.block.torch;

import net.minecraft.util.StringRepresentable;

/** The Carpenter's Torch styles, in the original's order; the hammer steps through them. */
public enum TorchType implements StringRepresentable {
    VANILLA("vanilla"),
    LANTERN("lantern");

    private final String name;

    TorchType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public TorchType next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public TorchType previous() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }
}
