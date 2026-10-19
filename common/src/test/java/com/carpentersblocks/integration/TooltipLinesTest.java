package com.carpentersblocks.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TooltipLinesTest {

    @Test
    void designNamesReadAsWords() {
        assertEquals("Offset Squares", TooltipLines.designName("stock_offset_squares"));
        assertEquals("Decorative2", TooltipLines.designName("stock_decorative2"));
        assertEquals("Creeper Face", TooltipLines.designName("creeper_face"));
        assertEquals("Brick", TooltipLines.designName("brick"));
    }
}
