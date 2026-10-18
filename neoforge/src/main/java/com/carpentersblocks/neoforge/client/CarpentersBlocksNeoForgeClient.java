package com.carpentersblocks.neoforge.client;

import java.util.Map;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import com.carpentersblocks.client.CoverTint;
import com.carpentersblocks.client.frame.ClientFrames;
import com.carpentersblocks.registry.ModContent;

public final class CarpentersBlocksNeoForgeClient {

    private CarpentersBlocksNeoForgeClient() {}

    public static void init(IEventBus modBus) {
        // Blocks exist only after registration, so code-built frames are registered at client setup
        modBus.addListener((FMLClientSetupEvent event) -> ClientFrames.registerAll());
        modBus.addListener(CarpentersBlocksNeoForgeClient::wrapModels);
        modBus.addListener(CarpentersBlocksNeoForgeClient::registerColors);
    }

    /** Wraps every block-state model of the coverable blocks; item models stay as baked. */
    private static void wrapModels(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (Block block : ModContent.coverableBlocks()) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                ModelResourceLocation id = BlockModelShaper.stateToModelLocation(state);
                BakedModel model = models.get(id);
                if (model != null && !(model instanceof NeoForgeCoverModel)) {
                    models.put(id, new NeoForgeCoverModel(model));
                }
            }
        }
    }

    private static void registerColors(RegisterColorHandlersEvent.Block event) {
        event.register(CoverTint.INSTANCE, ModContent.coverableBlocks().toArray(Block[]::new));
    }
}
