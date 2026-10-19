package com.carpentersblocks.neoforge.client;

import java.util.Map;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.client.CoverTint;
import com.carpentersblocks.client.SlopeSelectClient;
import com.carpentersblocks.client.entity.CarpentersTileRenderer;
import com.carpentersblocks.client.frame.ClientFrames;
import com.carpentersblocks.client.frame.SlopeItemModel;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.registry.TileContent;

public final class CarpentersBlocksNeoForgeClient {

    private CarpentersBlocksNeoForgeClient() {}

    public static void init(IEventBus modBus) {
        // Blocks exist only after registration, so code-built frames are registered at client setup
        modBus.addListener((FMLClientSetupEvent event) -> ClientFrames.registerAll());
        modBus.addListener(CarpentersBlocksNeoForgeClient::wrapModels);
        modBus.addListener(CarpentersBlocksNeoForgeClient::registerColors);
        modBus.addListener(CarpentersBlocksNeoForgeClient::registerRenderers);
        SlopeSelectClient.sender = PacketDistributor::sendToServer;
        NeoForge.EVENT_BUS.addListener(CarpentersBlocksNeoForgeClient::onScroll);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        if (TileContent.CARPENTERS_TILE != null) {
            event.registerEntityRenderer(TileContent.CARPENTERS_TILE, CarpentersTileRenderer::new);
        }
    }

    /** Slope select: scrolling while sneaking with a slope in hand changes its kind instead of the hotbar slot. */
    private static void onScroll(InputEvent.MouseScrollingEvent event) {
        if (SlopeSelectClient.onScroll(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    /** Wraps every block-state model of the coverable blocks; item models only where built in code. */
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
        for (ResourceLocation itemId : BuiltInRegistries.ITEM.keySet()) {
            if (CarpentersBlocks.MOD_ID.equals(itemId.getNamespace())) {
                ModelResourceLocation id = ModelResourceLocation.inventory(itemId);
                BakedModel model = models.get(id);
                if (model != null) {
                    BakedModel item = ClientFrames.itemModel(itemId, model);
                    models.put(id, item instanceof SlopeItemModel slopes ? new NeoForgeSlopeItemModel(slopes) : item);
                }
            }
        }
    }

    private static void registerColors(RegisterColorHandlersEvent.Block event) {
        event.register(CoverTint.INSTANCE, ModContent.coverableBlocks().toArray(Block[]::new));
    }
}
