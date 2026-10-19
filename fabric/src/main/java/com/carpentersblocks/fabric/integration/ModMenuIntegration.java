package com.carpentersblocks.fabric.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import com.carpentersblocks.client.config.ConfigScreen;
import com.carpentersblocks.fabric.config.FabricConfig;

/** Mod Menu: the config button opens {@link ConfigScreen}. Reached only through the {@code modmenu} entrypoint. */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen(parent, FabricConfig.values(), FabricConfig::save);
    }
}
