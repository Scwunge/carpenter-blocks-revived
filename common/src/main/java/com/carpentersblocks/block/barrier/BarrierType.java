package com.carpentersblocks.block.barrier;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

/**
 * Styles shared by the Carpenter's Barrier and Gate, in the order of the original's type values (the ordinal is the
 * original value). The vanilla style has three sub-types that lower the rails or use a single wide plank.
 */
public enum BarrierType implements StringRepresentable {

    VANILLA,
    VANILLA_X1,
    VANILLA_X2,
    VANILLA_X3,
    PICKET,
    SHADOWBOX,
    WALL;

    public boolean isVanilla() {
        return ordinal() <= VANILLA_X3.ordinal();
    }

    /** How far the rails of the vanilla sub-types are lowered, in pixels. */
    public int railDrop() {
        return isVanilla() ? ordinal() : 0;
    }

    /** Hammer right-click: vanilla, picket, shadowbox, wall, and back to vanilla. */
    public BarrierType nextType() {
        return isVanilla() ? PICKET : this == WALL ? VANILLA : values()[ordinal() + 1];
    }

    /** Hammer right-click while sneaking: the next vanilla sub-type; other styles have none. */
    public BarrierType nextSubType() {
        return isVanilla() ? values()[(ordinal() + 1) % (VANILLA_X3.ordinal() + 1)] : this;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
