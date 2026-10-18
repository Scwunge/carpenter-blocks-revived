package com.carpentersblocks.block.coverable;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;

/**
 * The chisel designs that can be carved into a cover. The original found these by scanning its texture folder; the
 * list is fixed here, in the same (alphabetical) order, so cycling matches.
 */
public final class ChiselDesigns {

    public static final List<String> ALL = List.of("brick", "brick_slab", "brick_slab_staggered",
            "brick_slab_staggered_vertical", "brick_slab_vertical", "chiseled", "column", "column_top",
            "creeper_face");

    private ChiselDesigns() {}

    public static boolean isValid(@Nullable String design) {
        return design != null && ALL.contains(design);
    }

    /** Next design after {@code design}, wrapping around; the first design when none is set. */
    public static String next(@Nullable String design) {
        int idx = design == null ? -1 : ALL.indexOf(design);
        return ALL.get((idx + 1) % ALL.size());
    }

    /** Design before {@code design}, wrapping around; the last design when none is set. */
    public static String prev(@Nullable String design) {
        int idx = design == null ? -1 : ALL.indexOf(design);
        return ALL.get(idx <= 0 ? ALL.size() - 1 : idx - 1);
    }

    public static ResourceLocation texture(String design) {
        return CarpentersBlocks.id("block/designs/chisel/" + design);
    }
}
