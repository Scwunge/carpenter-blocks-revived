package com.carpentersblocks.block.slope;

import java.util.Locale;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * The five slope items, as the original's item damage values (the ordinal). Which slope gets placed follows from the
 * kind and how the player places it.
 */
public enum SlopeKind implements StringRepresentable {
    WEDGE(Slope.WEDGE_POS_W),
    OBLIQUE_INTERIOR(Slope.OBL_INT_POS_SW),
    OBLIQUE_EXTERIOR(Slope.OBL_EXT_POS_SW),
    PRISM(Slope.PRISM_POS),
    PRISM_WEDGE(Slope.PRISM_WEDGE_POS_W);

    public static final Codec<SlopeKind> CODEC = StringRepresentable.fromEnum(SlopeKind::values);
    public static final StreamCodec<ByteBuf, SlopeKind> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(SlopeKind::byId, SlopeKind::ordinal);

    private static final SlopeKind[] VALUES = values();

    /** The slope the item shows in hand and in inventories. */
    public final Slope display;

    SlopeKind(Slope display) {
        this.display = display;
    }

    public static SlopeKind byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : WEDGE;
    }

    /** The item kind a slope belongs to; hammering can change a slope's type, and the drop follows it. */
    public static SlopeKind of(Slope slope) {
        return switch (slope.type) {
            case OBLIQUE_INT -> OBLIQUE_INTERIOR;
            case OBLIQUE_EXT -> OBLIQUE_EXTERIOR;
            case PRISM, PRISM_1P, PRISM_2P, PRISM_3P, PRISM_4P -> PRISM;
            case PRISM_WEDGE -> PRISM_WEDGE;
            default -> WEDGE;
        };
    }

    /** The next kind for slope select (sneak + scroll), wrapping around. */
    public SlopeKind cycle(boolean forward) {
        return VALUES[Math.floorMod(ordinal() + (forward ? 1 : -1), VALUES.length)];
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
