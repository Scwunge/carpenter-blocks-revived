package com.carpentersblocks.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

import com.carpentersblocks.config.ConfigOptions;
import com.carpentersblocks.config.ConfigOptions.Option;

/** Generated recipes with a recipeQuantity* option give that option's default, so nothing changes out of the box. */
class RecipeQuantityTest {

    @Test
    void configuredCountsMatchTheDefaults() throws IOException, URISyntaxException {
        Path dir = Path.of(RecipeQuantityTest.class.getResource("/data/carpentersblocks/recipe").toURI());
        List<Path> files;
        try (Stream<Path> stream = Files.list(dir)) {
            files = stream.filter(file -> file.toString().endsWith(".json")).toList();
        }
        int configured = 0;
        for (Path file : files) {
            JsonObject recipe;
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                recipe = JsonParser.parseReader(reader).getAsJsonObject();
            }
            if (!recipe.has("quantity_option")) {
                continue;
            }
            ++configured;
            assertEquals("carpentersblocks:configured_shaped", recipe.get("type").getAsString(), file.toString());
            Option option = option(recipe.get("quantity_option").getAsString());
            assertNotNull(option, file.toString());
            assertEquals(option.defaultValue(), recipe.getAsJsonObject("result").get("count").getAsInt(),
                    file.getFileName().toString());
        }
        assertFalse(configured == 0, "no configured recipes found");
    }

    private static Option option(String key) {
        for (String section : ConfigOptions.SECTIONS) {
            for (Option option : ConfigOptions.inSection(section)) {
                if (option.key().equals(key)) {
                    return option;
                }
            }
        }
        return null;
    }
}
