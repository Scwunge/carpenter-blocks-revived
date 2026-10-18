package com.carpentersblocks.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.Direction;

import org.junit.jupiter.api.Test;

class FrameShapeTest {

    @Test
    void fullBlockCoversEveryFace() {
        for (Direction face : Direction.values()) {
            assertTrue(FrameShape.FULL.coversFace(face), face.getName());
        }
    }

    @Test
    void slabCoversOnlyTheFaceItRestsAgainst() {
        for (Direction side : Direction.values()) {
            FrameShape slab = FrameShape.slabAgainst(side);
            assertEquals(side.getName(), slab.getSerializedName());
            for (Direction face : Direction.values()) {
                assertEquals(face == side, slab.coversFace(face), side.getName() + " slab, " + face.getName() + " face");
            }
        }
    }

    @Test
    void slabIsHalfABlockThick() {
        for (Direction side : Direction.values()) {
            var bounds = FrameShape.slabAgainst(side).shape().bounds();
            double thickness = bounds.max(side.getAxis()) - bounds.min(side.getAxis());
            assertEquals(0.5, thickness, 1e-9, side.getName());
            assertFalse(FrameShape.slabAgainst(side).shape().isEmpty());
        }
    }
}
