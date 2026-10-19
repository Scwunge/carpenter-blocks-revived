package com.carpentersblocks.neoforge.client.dev;

import java.util.Map;
import java.util.function.UnaryOperator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.common.NeoForge;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.client.dev.AutoShots;

/** Dev-only: runs an {@link AutoShots} script when one is given, with NeoForge's mod list and our config screen. */
public final class NeoForgeAutoShots {

    private NeoForgeAutoShots() {}

    public static void registerIfRequested() {
        AutoShots shots = AutoShots.fromProperty(Map.of("mods", ModListScreen::new, "config", NeoForgeAutoShots::config),
                ModList.get()::isLoaded);
        if (shots != null) {
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> shots.tick(Minecraft.getInstance()));
        }
    }

    /** Our config screen as the mod list finds it, through the registered extension point. */
    private static Screen config(Screen parent) {
        ModContainer container = ModList.get().getModContainerById(CarpentersBlocks.MOD_ID).orElseThrow();
        return container.getCustomExtension(IConfigScreenFactory.class).orElseThrow()
                .createScreen(container, parent);
    }
}
