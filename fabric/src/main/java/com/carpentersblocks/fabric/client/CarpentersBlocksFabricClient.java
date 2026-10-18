package com.carpentersblocks.fabric.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.carpentersblocks.client.CoverTint;
import com.carpentersblocks.registry.ModContent;

public class CarpentersBlocksFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        List<Block> blocks = ModContent.coverableBlocks();
        ColorProviderRegistry.BLOCK.register(CoverTint.INSTANCE, blocks.toArray(Block[]::new));

        // Wrap every block-state model of the coverable blocks; item models stay as baked
        Set<ResourceLocation> ids = new HashSet<>();
        blocks.forEach(block -> ids.add(BuiltInRegistries.BLOCK.getKey(block)));
        ModelLoadingPlugin.register(context -> context.modifyModelAfterBake().register((model, modelContext) -> {
            ModelResourceLocation id = modelContext.topLevelId();
            if (id != null && !ModelResourceLocation.INVENTORY_VARIANT.equals(id.variant()) && ids.contains(id.id())) {
                return new FabricCoverModel(model);
            }
            return model;
        }));
    }
}
