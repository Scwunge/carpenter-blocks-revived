package com.carpentersblocks.fabric.client.dev;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.client.dev.AutoShots;

/** Dev-only: runs an {@link AutoShots} script when one is given; with Mod Menu, adds its mod list and our config. */
public final class FabricAutoShots {

    private FabricAutoShots() {}

    public static void registerIfRequested() {
        Map<String, UnaryOperator<Screen>> screens = new HashMap<>();
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            screens.put("mods", ModMenuScreens::mods);
            screens.put("config", ModMenuScreens::config);
        }
        AutoShots shots = AutoShots.fromProperty(screens, FabricLoader.getInstance()::isModLoaded);
        if (shots != null) {
            ClientTickEvents.END_CLIENT_TICK.register(shots::tick);
        }
    }

    /** Apart, so Mod Menu's classes load only when it is there. */
    private static final class ModMenuScreens {

        static Screen mods(Screen parent) {
            return com.terraformersmc.modmenu.api.ModMenuApi.createModsScreen(parent);
        }

        /** Our config screen as Mod Menu finds it, through the modmenu entrypoint. */
        static Screen config(Screen parent) {
            return com.terraformersmc.modmenu.ModMenu.getConfigScreen(CarpentersBlocks.MOD_ID, parent);
        }
    }
}
