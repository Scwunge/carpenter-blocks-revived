package com.carpentersblocks.block.design;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.carpentersblocks.CarpentersBlocks;

/**
 * The designs one kind of block can show (bed quilts, flower pot glazes), stored by name in the block entity. The
 * original found these by scanning its texture folders; each list is fixed here in the same (alphabetical) order, so
 * cycling matches. Port of the original {@code DesignHandler} for block designs.
 *
 * @param folder the texture folder under {@code block/designs/}
 * @param names  the designs, in cycling order
 */
public record DesignList(String folder, List<String> names) {

    /** Bed quilts: a 48 by 64 template per design, cut up by {@code BedFrames}. */
    public static final DesignList BED = new DesignList("bed", List.of("carpenters_hammer", "carpenters_quilt_green",
            "carpenters_quilt_multicolor", "carpenters_quilt_orange", "carpenters_rainbow", "carpenters_snowflake"));

    /** Flower pot glazes, drawn in place of the cover. */
    public static final DesignList FLOWER_POT = new DesignList("flower_pot", List.of("black", "blue", "brown", "cyan",
            "gray", "green", "light_blue", "light_gray", "lime", "magenta", "orange", "original", "pink", "purple",
            "rainbow", "red", "white", "yellow"));

    public boolean isValid(String design) {
        return names.contains(design);
    }

    /** The design after {@code design}, wrapping around; the first one when none ({@code ""}) is set. */
    public String next(String design) {
        int idx = names.indexOf(design) + 1;
        return names.get(idx >= names.size() ? 0 : idx);
    }

    /** The design before {@code design}, wrapping around; the last one when none is set. */
    public String previous(String design) {
        int idx = design.isEmpty() ? names.size() - 1 : names.indexOf(design) - 1;
        return names.get(idx < 0 ? names.size() - 1 : idx);
    }

    public ResourceLocation texture(String design) {
        return CarpentersBlocks.id("block/designs/" + folder + "/" + design);
    }
}
