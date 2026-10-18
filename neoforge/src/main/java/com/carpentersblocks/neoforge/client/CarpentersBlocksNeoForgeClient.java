package com.carpentersblocks.neoforge.client;

import java.util.Map;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import com.carpentersblocks.client.CoverTint;
import com.carpentersblocks.registry.ModContent;

public final class CarpentersBlocksNeoForgeClient {

    private CarpentersBlocksNeoForgeClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(CarpentersBlocksNeoForgeClient::wrapModels);
        modBus.addListener(CarpentersBlocksNeoForgeClient::registerColors);
    }

    /** Wraps every block-state model of the Carpenter's Block; the item model stays as baked. */
    private static void wrapModels(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (BlockState state : ModContent.CARPENTERS_BLOCK.getStateDefinition().getPossibleStates()) {
            ModelResourceLocation id = BlockModelShaper.stateToModelLocation(state);
            BakedModel model = models.get(id);
            if (model != null && !(model instanceof NeoForgeCoverModel)) {
                models.put(id, new NeoForgeCoverModel(model));
            }
        }
    }

    private static void registerColors(RegisterColorHandlersEvent.Block event) {
        event.register(CoverTint.INSTANCE, ModContent.CARPENTERS_BLOCK);
    }
}
