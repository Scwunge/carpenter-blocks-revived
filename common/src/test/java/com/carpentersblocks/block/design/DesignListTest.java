package com.carpentersblocks.block.design;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DesignListTest {

    @Test
    void cyclingWrapsBothWays() {
        DesignList list = DesignList.FLOWER_POT;
        assertEquals("black", list.next(""));
        assertEquals("yellow", list.previous(""));
        assertEquals("black", list.next("yellow"));
        assertEquals("yellow", list.previous("black"));
        assertEquals("blue", list.next("black"));
    }

    @Test
    void tileDesignsNeverCycleBackToBlank() {
        DesignList list = DesignList.TILE;
        assertEquals("stock_chain", list.next(""));
        assertEquals("stock_striped", list.previous(""));
        assertEquals("stock_chain", list.next("stock_striped"));
        assertEquals("carpentersblocks:block/designs/tile/stock_dots", list.texture("stock_dots").toString());
    }

    @Test
    void namesAndTextures() {
        assertTrue(DesignList.BED.isValid("carpenters_rainbow"));
        assertFalse(DesignList.BED.isValid(""));
        assertEquals("carpentersblocks:block/designs/flower_pot/red",
                DesignList.FLOWER_POT.texture("red").toString());
    }
}
