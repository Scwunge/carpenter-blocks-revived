package com.carpentersblocks.client.frame;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.registry.BarrierContent;
import com.carpentersblocks.registry.SlopeContent;

/** Registers every code-built frame. Both loaders' client entry points call this once during client setup. */
public final class ClientFrames {

    private ClientFrames() {}

    public static void registerAll() {
        if (SlopeContent.CARPENTERS_SLOPE != null) {
            FrameModels.register(SlopeContent.CARPENTERS_SLOPE, SlopeFrames.INSTANCE);
        }
        if (BarrierContent.CARPENTERS_BARRIER != null) {
            FrameModels.register(BarrierContent.CARPENTERS_BARRIER, BarrierFrames.BARRIER);
        }
        if (BarrierContent.CARPENTERS_GATE != null) {
            FrameModels.register(BarrierContent.CARPENTERS_GATE, BarrierFrames.GATE);
        }
    }

    /** The item model to use for an item's baked inventory model; items with code-built models get theirs. */
    public static BakedModel itemModel(ResourceLocation itemId, BakedModel baked) {
        if (itemId.equals(CarpentersBlocks.id("carpenters_slope"))) {
            return new SlopeItemModel(baked);
        }
        return baked;
    }
}
