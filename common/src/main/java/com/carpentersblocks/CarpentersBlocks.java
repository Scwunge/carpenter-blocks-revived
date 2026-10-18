package com.carpentersblocks;

import net.minecraft.resources.ResourceLocation;

public final class CarpentersBlocks {

    public static final String MOD_ID = "carpentersblocks";

    private CarpentersBlocks() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
