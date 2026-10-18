package com.carpentersblocks.block.torch;

import net.minecraft.util.StringRepresentable;

/** How well a Carpenter's Torch burns: rain puts it out a step at a time, and it recovers when dry. */
public enum TorchFlame implements StringRepresentable {
    LIT("lit", 14),
    SMOLDERING("smoldering", 9),
    UNLIT("unlit", 0);

    private final String name;
    private final int light;

    /** @param light as the original: a vanilla torch's light when lit, two thirds of it when smoldering */
    TorchFlame(String name, int light) {
        this.name = name;
        this.light = light;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public int light() {
        return light;
    }

    /** The flame after a random tick, wet meaning rain is falling on the torch, as the original's {@code updateTick}. */
    public TorchFlame weather(boolean wet) {
        return switch (this) {
            case LIT -> wet ? SMOLDERING : LIT;
            case SMOLDERING -> wet ? UNLIT : LIT;
            case UNLIT -> wet ? UNLIT : SMOLDERING;
        };
    }
}
