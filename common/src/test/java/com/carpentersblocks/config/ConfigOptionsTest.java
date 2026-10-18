package com.carpentersblocks.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.carpentersblocks.config.ConfigOptions.Option;

class ConfigOptionsTest {

    @Test
    void everyConfigFieldHasAnOption() {
        assertEquals(Set.of(), ConfigOptions.missingFields());
    }

    @Test
    void keysAreUniqueAndSectionsKnown() {
        Set<String> keys = new HashSet<>();
        for (Option option : ConfigOptions.all()) {
            assertTrue(keys.add(option.key()), "duplicate key " + option.key());
            assertTrue(ConfigOptions.SECTIONS.contains(option.section()), option.key());
            assertFalse(option.comment().isEmpty(), option.key());
        }
    }

    @Test
    void defaultsMatchOriginalMod() {
        Map<String, Object> expected = Map.of(
                "slopeSmoothness", 2,
                "multiBlockSizeLimit", 500,
                "toolUses", 400,
                "toolsDamageable", true,
                "enableRoutableFluids", false,
                "enableFreeStandingLadders", false,
                "recipeQuantityBlock", 5,
                "recipeQuantitySlope", 6,
                "recipeQuantityTile", 12);
        for (Option option : ConfigOptions.all()) {
            if (expected.containsKey(option.key())) {
                assertEquals(expected.get(option.key()), option.defaultValue(), option.key());
            }
        }
    }

    @Test
    void integersAreClampedIntoRange() {
        Option smoothness = option("slopeSmoothness");
        try {
            smoothness.set(1000);
            assertEquals(smoothness.max(), CBConfig.slopeSmoothness);
            smoothness.set(-5);
            assertEquals(smoothness.min(), CBConfig.slopeSmoothness);
        } finally {
            smoothness.set(smoothness.defaultValue());
        }
    }

    private static Option option(String key) {
        return ConfigOptions.all().stream().filter(o -> o.key().equals(key)).findFirst().orElseThrow();
    }
}
