package com.carpentersblocks.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes every {@link CBConfig} value once: the section and key it is stored under, its comment and allowed range.
 * Both loaders build their config file (NeoForge TOML, Fabric JSON) from this list and write the values they read back
 * into {@link CBConfig}, so the two files always have the same options. Sections follow the original's
 * {@code features}, {@code blocks}, {@code items} and {@code recipe quantities} categories.
 */
public final class ConfigOptions {

    public static final String FEATURES = "features";
    public static final String BLOCKS = "blocks";
    public static final String ITEMS = "items";
    public static final String RECIPE_QUANTITIES = "recipe_quantities";

    public static final List<String> SECTIONS = List.of(FEATURES, BLOCKS, ITEMS, RECIPE_QUANTITIES);

    private static final List<Option> OPTIONS = new ArrayList<>();

    static {
        // Features (original FeatureRegistry)
        bool(FEATURES, "enableCovers", "Allow blocks to be applied as covers.");
        bool(FEATURES, "enableOverlays", "Allow overlays (grass, snow, web, vine, hay, mycelium) on covered blocks.");
        bool(FEATURES, "enableSideCovers", "Allow a different cover on each side of a block.");
        bool(FEATURES, "enableDyeColors", "Allow dyes to color covered blocks.");
        bool(FEATURES, "enableChiselDesigns", "Allow the chisel to carve designs into covers.");
        bool(FEATURES, "enableTorchWeatherEffects",
                "Torches extinguish themselves when exposed to rain or snow.");
        bool(FEATURES, "enableOwnership",
                "Prevent players other than the owner and server operators from editing your objects.",
                "This does not protect objects against destruction, and may allow activation where appropriate.",
                "The Carpenter's Safe is not affected by this.");
        bool(FEATURES, "enableIllumination", "Allow glowstone dust to make blocks give off light.");
        bool(FEATURES, "enableRoutableFluids",
                "Unobstructed stationary fluid next to a block renders in the block space.",
                "May cause noticeable chunk update lag spikes.");
        bool(FEATURES, "enableAlphaPanes",
                "Render panes (used in doors, hatches and other blocks) translucent, e.g. for stained glass.");
        bool(FEATURES, "enableRailSlopes",
                "Carpenter's blocks with a solid top grow a slope under sloping rails above them.");
        bool(FEATURES, "enableGarageDoorVoidFill",
                "Garage doors fill in gaps when barriers beneath them are destroyed.");
        bool(FEATURES, "enableFreeStandingLadders", "Ladders can be placed without a supporting block.");
        integer(FEATURES, "slopeSmoothness", 1, 64,
                "Smoothness of slope faces. 2 looks like stairs; values above 25 are generally fluid.");
        integer(FEATURES, "multiBlockSizeLimit", 1, 100_000,
                "How many blocks can be connected as a single object. Only applies to garage doors.");

        // Blocks (original BlockRegistry)
        for (String block : List.of("Barrier", "Bed", "Block", "Button", "CollapsibleBlock", "DaylightSensor", "Door",
                "FlowerPot", "GarageDoor", "Gate", "Hatch", "Ladder", "Lever", "PressurePlate", "Safe", "Slope",
                "Stairs", "Torch")) {
            bool(BLOCKS, "enable" + block, "Register the Carpenter's " + spaced(block) + ".");
            integer(RECIPE_QUANTITIES, "recipeQuantity" + block, 1, 64,
                    "Carpenter's " + spaced(block) + " crafted per recipe.");
        }

        // Items (original ItemRegistry)
        bool(ITEMS, "enableHammer", "Register the Carpenter's Hammer.");
        bool(ITEMS, "enableChisel", "Register the Carpenter's Chisel.");
        bool(ITEMS, "enableTile", "Register the Carpenter's Tile.");
        integer(ITEMS, "toolUses", 1, 100_000, "Uses before a hammer or chisel breaks.");
        bool(ITEMS, "toolsDamageable", "Hammers and chisels take damage when used.");
        decimal(ITEMS, "itemHammerDamageChanceFromSlopes", 0, 1,
                "Chance that the hammer takes damage when it rotates or alters a slope.");
        decimal(ITEMS, "itemHammerDamageChanceFromStairs", 0, 1,
                "Chance that the hammer takes damage when it rotates or alters stairs.");
        decimal(ITEMS, "itemHammerDamageChanceFromCollapsible", 0, 1,
                "Chance that the hammer takes damage when it adjusts a collapsible block.");
        integer(RECIPE_QUANTITIES, "recipeQuantityTile", 1, 64, "Carpenter's Tiles crafted per recipe.");
    }

    private ConfigOptions() {}

    public static List<Option> all() {
        return List.copyOf(OPTIONS);
    }

    public static List<Option> inSection(String section) {
        return OPTIONS.stream().filter(option -> option.section().equals(section)).toList();
    }

    /** Public static fields of {@link CBConfig} that have no option here; empty when the table is complete. */
    public static Set<String> missingFields() {
        Set<String> missing = new LinkedHashSet<>();
        for (Field field : CBConfig.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                missing.add(field.getName());
            }
        }
        OPTIONS.forEach(option -> missing.remove(option.key()));
        return missing;
    }

    /**
     * One config value, backed by the {@link CBConfig} field of the same name.
     *
     * @param min, max inclusive range for integer and decimal options; unused for booleans
     */
    public record Option(String section, String key, Type type, Object defaultValue, double min, double max,
            List<String> comment, Field field) {

        public enum Type { BOOLEAN, INTEGER, DOUBLE }

        public Object get() {
            try {
                return field.get(null);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        /** Stores a value read from a config file, clamping numbers into range. */
        public void set(Object value) {
            try {
                switch (type) {
                    case BOOLEAN -> field.setBoolean(null, (Boolean) value);
                    case INTEGER -> field.setInt(null, (int) Math.clamp(((Number) value).longValue(), (long) min,
                            (long) max));
                    case DOUBLE -> field.setDouble(null, Math.clamp(((Number) value).doubleValue(), min, max));
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static void bool(String section, String key, String... comment) {
        add(section, key, Option.Type.BOOLEAN, 0, 0, comment);
    }

    private static void integer(String section, String key, int min, int max, String... comment) {
        add(section, key, Option.Type.INTEGER, min, max, comment);
    }

    private static void decimal(String section, String key, double min, double max, String... comment) {
        add(section, key, Option.Type.DOUBLE, min, max, comment);
    }

    private static void add(String section, String key, Option.Type type, double min, double max,
            String... comment) {
        Field field;
        try {
            field = CBConfig.class.getField(key);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("CBConfig has no field " + key, e);
        }
        Option option = new Option(section, key, type, null, min, max, List.of(comment), field);
        // The field's initial value is the original mod's default
        OPTIONS.add(new Option(section, key, type, option.get(), min, max, List.of(comment), field));
    }

    /** {@code "CollapsibleBlock"} -> {@code "Collapsible Block"}. */
    private static String spaced(String name) {
        return name.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
    }
}
