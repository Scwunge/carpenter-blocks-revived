package com.carpentersblocks.neoforge.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import com.carpentersblocks.config.CBConfig;
import com.carpentersblocks.config.ConfigOptions;
import com.carpentersblocks.config.ConfigOptions.Option;

/**
 * Fills {@link CBConfig} from {@code config/carpentersblocks-startup.toml}. A STARTUP config is read as soon as it is
 * registered, which is needed because block and item enable flags are used during registration.
 */
public final class NeoForgeConfig {

    private NeoForgeConfig() {}

    /** Call first thing in the mod constructor. */
    public static void load() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        Map<Option, Supplier<?>> values = new LinkedHashMap<>();
        for (String section : ConfigOptions.SECTIONS) {
            builder.push(section);
            for (Option option : ConfigOptions.inSection(section)) {
                builder.comment(option.comment().toArray(String[]::new));
                values.put(option, switch (option.type()) {
                    case BOOLEAN -> builder.define(option.key(), (boolean) option.defaultValue());
                    case INTEGER -> builder.defineInRange(option.key(), (int) option.defaultValue(),
                            (int) option.min(), (int) option.max());
                    case DOUBLE -> builder.defineInRange(option.key(), (double) option.defaultValue(), option.min(),
                            option.max());
                });
            }
            builder.pop();
        }
        ModConfigSpec spec = builder.build();

        ModContainer container = ModLoadingContext.get().getActiveContainer();
        container.registerConfig(ModConfig.Type.STARTUP, spec);
        values.forEach((option, value) -> option.set(value.get()));
    }
}
