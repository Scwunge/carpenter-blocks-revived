package com.carpentersblocks.fabric.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carpentersblocks.config.CBConfig;
import com.carpentersblocks.config.ConfigOptions;
import com.carpentersblocks.config.ConfigOptions.Option;

/**
 * Fills {@link CBConfig} from {@code config/carpentersblocks.json}, then rewrites the file so it always lists every
 * option. Comments live in {@code _comment} entries next to each section since JSON has none. Missing or invalid
 * values fall back to their defaults.
 */
public final class FabricConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("carpentersblocks");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String COMMENT = "_comment";

    private FabricConfig() {}

    /** Call first thing in {@code onInitialize}. */
    public static void load() {
        load(file());
    }

    static void load(Path file) {
        Map<Option, Object> values = values(file);
        values.forEach(Option::set);
        save(file, values);
    }

    /**
     * The values in the file now, which can differ from {@link CBConfig} after the config screen saved changes that
     * apply at the next start.
     */
    public static Map<Option, Object> values() {
        return values(file());
    }

    /** Writes these values (every option) to the file; they take effect at the next start. */
    public static void save(Map<Option, Object> values) {
        save(file(), values);
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("carpentersblocks.json");
    }

    private static Map<Option, Object> values(Path file) {
        JsonObject root = read(file);
        Map<Option, Object> values = new LinkedHashMap<>();
        for (String section : ConfigOptions.SECTIONS) {
            JsonObject in = root.has(section) && root.get(section).isJsonObject()
                    ? root.getAsJsonObject(section) : new JsonObject();
            for (Option option : ConfigOptions.inSection(section)) {
                values.put(option, valueOf(option, in.get(option.key())));
            }
        }
        return values;
    }

    private static void save(Path file, Map<Option, Object> values) {
        JsonObject out = new JsonObject();
        for (String section : ConfigOptions.SECTIONS) {
            JsonObject written = new JsonObject();
            JsonObject comments = new JsonObject();
            for (Option option : ConfigOptions.inSection(section)) {
                comments.addProperty(option.key(), describe(option));
                written.add(option.key(), toJson(option.clamp(values.getOrDefault(option, option.defaultValue()))));
            }
            out.add(section, withComments(comments, written));
        }
        write(file, out);
    }

    private static Object valueOf(Option option, JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (option.type() == Option.Type.BOOLEAN && primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
            if (option.type() == Option.Type.INTEGER && primitive.isNumber()) {
                return primitive.getAsLong();
            }
            if (option.type() == Option.Type.DOUBLE && primitive.isNumber()) {
                return primitive.getAsDouble();
            }
        }
        if (element != null) {
            LOGGER.warn("Config option {} has invalid value {}, using {}", option.key(), element, option.defaultValue());
        }
        return option.defaultValue();
    }

    private static String describe(Option option) {
        String text = String.join(" ", option.comment());
        return switch (option.type()) {
            case BOOLEAN -> text + " Default " + option.defaultValue() + ".";
            case INTEGER -> text + " Range: " + (int) option.min() + "-" + (int) option.max() + ", default "
                    + option.defaultValue() + ".";
            case DOUBLE -> text + " Range: " + option.min() + "-" + option.max() + ", default "
                    + option.defaultValue() + ".";
        };
    }

    private static JsonElement toJson(Object value) {
        return value instanceof Boolean b ? new JsonPrimitive(b) : new JsonPrimitive((Number) value);
    }

    private static JsonObject withComments(JsonObject comments, JsonObject values) {
        JsonObject section = new JsonObject();
        section.add(COMMENT, comments);
        values.entrySet().forEach(entry -> section.add(entry.getKey(), entry.getValue()));
        return section;
    }

    private static JsonObject read(Path file) {
        if (!Files.exists(file)) {
            return new JsonObject();
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            return element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
        } catch (IOException | JsonParseException e) {
            LOGGER.error("Could not read {}, using defaults", file, e);
            return new JsonObject();
        }
    }

    private static void write(Path file, JsonObject root) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Could not write {}", file, e);
        }
    }
}
