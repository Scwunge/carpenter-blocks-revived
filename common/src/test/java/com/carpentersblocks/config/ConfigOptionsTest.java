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
        Map<String, Object> expected = Map.ofEntries(
                Map.entry("slopeSmoothness", 2),
                Map.entry("multiBlockSizeLimit", 500),
                Map.entry("toolUses", 400),
                Map.entry("toolsDamageable", true),
                Map.entry("enableRoutableFluids", false),
                Map.entry("enableFreeStandingLadders", false),
                Map.entry("recipeQuantityBlock", 5),
                Map.entry("recipeQuantitySlope", 6),
                Map.entry("recipeQuantityTile", 12),
                Map.entry("itemHammerDamageChanceFromSlopes", 0.75),
                Map.entry("itemHammerDamageChanceFromStairs", 1.0),
                Map.entry("itemHammerDamageChanceFromCollapsible", 0.2));
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
        Option stairs = option("itemHammerDamageChanceFromStairs");
        try {
            stairs.set(2.5);
            assertEquals(1.0, CBConfig.itemHammerDamageChanceFromStairs);
            stairs.set(0.3);
            assertEquals(0.3, CBConfig.itemHammerDamageChanceFromStairs);
        } finally {
            stairs.set(stairs.defaultValue());
        }
    }

    private static Option option(String key) {
        return ConfigOptions.all().stream().filter(o -> o.key().equals(key)).findFirst().orElseThrow();
    }
}
