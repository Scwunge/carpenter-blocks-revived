package com.carpentersblocks.block.flowerpot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PotGeometryTest {

    @Test
    void theSoilAreaIsTheMiddleOfTheTop() {
        assertTrue(PotGeometry.onSoil(0.5, 0.5));
        assertFalse(PotGeometry.onSoil(0.34, 0.5));
        assertFalse(PotGeometry.onSoil(0.5, 0.65));
    }

    @Test
    void anglesFollowTheOriginal() {
        assertEquals(8, PotGeometry.angle(0));
        assertEquals(0, PotGeometry.angle(180));
        assertEquals(12, PotGeometry.angle(90));
        assertEquals(4, PotGeometry.angle(-90));
        assertEquals((float) Math.PI, PotGeometry.radians(8), 1e-6);
    }

    @Test
    void stackedPlantsSitOnTheSoil() {
        assertEquals(PotGeometry.PLANT_BASE, PotGeometry.STACKED.get(0).y1());
        assertEquals(PotGeometry.SOIL.y2(), PotGeometry.PLANT_BASE);
        assertEquals(16, PotGeometry.STACKED.get(2).y2());
    }
}
