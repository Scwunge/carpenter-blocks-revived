package com.carpentersblocks.neoforge.integration;

import me.shedaniel.rei.forge.REIPluginClient;

import com.carpentersblocks.integration.rei.ReiPlugin;

/** REI on NeoForge finds client plugins by this annotation; the plugin itself is shared with Fabric. */
@REIPluginClient
public class NeoForgeReiPlugin extends ReiPlugin {
}
