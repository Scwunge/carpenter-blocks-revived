package com.carpentersblocks.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

import com.carpentersblocks.config.ConfigOptions.Option;

/** The config screens (NeoForge's and the Fabric one) name every option and section through en_us. */
class ConfigLangTest {

    @Test
    void everyOptionHasAName() throws IOException {
        JsonObject lang;
        try (Reader reader = new InputStreamReader(ConfigLangTest.class.getResourceAsStream(
                "/assets/carpentersblocks/lang/en_us.json"), StandardCharsets.UTF_8)) {
            lang = JsonParser.parseReader(reader).getAsJsonObject();
        }
        for (String section : ConfigOptions.SECTIONS) {
            assertTrue(lang.has("carpentersblocks.configuration." + section), section);
            for (Option option : ConfigOptions.inSection(section)) {
                assertTrue(lang.has("carpentersblocks.configuration." + option.key()), option.key());
            }
        }
    }
}
