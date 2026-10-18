package com.carpentersblocks.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import com.carpentersblocks.client.CoverTint;
import com.carpentersblocks.registry.ModContent;

public class CarpentersBlocksFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.BLOCK.register(CoverTint.INSTANCE, ModContent.CARPENTERS_BLOCK);

        // Wrap every block-state model of the Carpenter's Block; the item model stays as baked
        ModelLoadingPlugin.register(context -> context.modifyModelAfterBake().register((model, modelContext) -> {
            ModelResourceLocation id = modelContext.topLevelId();
            if (id != null && !ModelResourceLocation.INVENTORY_VARIANT.equals(id.variant())
                    && id.id().equals(BuiltInRegistries.BLOCK.getKey(ModContent.CARPENTERS_BLOCK))) {
                return new FabricCoverModel(model);
            }
            return model;
        }));
    }
}
