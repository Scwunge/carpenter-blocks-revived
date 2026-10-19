package com.carpentersblocks.client.frame;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.registry.BarrierContent;
import com.carpentersblocks.registry.BedContent;
import com.carpentersblocks.registry.CollapsibleContent;
import com.carpentersblocks.registry.DoorContent;
import com.carpentersblocks.registry.FlowerPotContent;
import com.carpentersblocks.registry.LadderContent;
import com.carpentersblocks.registry.RedstoneContent;
import com.carpentersblocks.registry.SafeContent;
import com.carpentersblocks.registry.SlopeContent;
import com.carpentersblocks.registry.TorchContent;

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
        if (RedstoneContent.CARPENTERS_BUTTON != null) {
            FrameModels.register(RedstoneContent.CARPENTERS_BUTTON, RedstoneFrames.BUTTON);
        }
        if (RedstoneContent.CARPENTERS_LEVER != null) {
            FrameModels.register(RedstoneContent.CARPENTERS_LEVER, RedstoneFrames.LEVER_FRAME);
        }
        if (RedstoneContent.CARPENTERS_PRESSURE_PLATE != null) {
            FrameModels.register(RedstoneContent.CARPENTERS_PRESSURE_PLATE, RedstoneFrames.PRESSURE_PLATE);
        }
        if (RedstoneContent.CARPENTERS_DAYLIGHT_SENSOR != null) {
            FrameModels.register(RedstoneContent.CARPENTERS_DAYLIGHT_SENSOR, RedstoneFrames.DAYLIGHT_SENSOR);
        }
        if (TorchContent.CARPENTERS_TORCH != null) {
            FrameModels.register(TorchContent.CARPENTERS_TORCH, TorchFrames.TORCH);
        }
        if (LadderContent.CARPENTERS_LADDER != null) {
            FrameModels.register(LadderContent.CARPENTERS_LADDER, LadderFrames.LADDER);
        }
        if (DoorContent.CARPENTERS_DOOR != null) {
            FrameModels.register(DoorContent.CARPENTERS_DOOR, DoorFrames.DOOR);
        }
        if (DoorContent.CARPENTERS_HATCH != null) {
            FrameModels.register(DoorContent.CARPENTERS_HATCH, DoorFrames.HATCH);
        }
        if (DoorContent.CARPENTERS_GARAGE_DOOR != null) {
            FrameModels.register(DoorContent.CARPENTERS_GARAGE_DOOR, DoorFrames.GARAGE_DOOR);
        }
        if (BedContent.CARPENTERS_BED != null) {
            FrameModels.register(BedContent.CARPENTERS_BED, BedFrames.BED);
        }
        if (CollapsibleContent.CARPENTERS_COLLAPSIBLE_BLOCK != null) {
            FrameModels.register(CollapsibleContent.CARPENTERS_COLLAPSIBLE_BLOCK, CollapsibleFrames.COLLAPSIBLE);
        }
        if (FlowerPotContent.CARPENTERS_FLOWER_POT != null) {
            FrameModels.register(FlowerPotContent.CARPENTERS_FLOWER_POT, FlowerPotFrames.POT);
            FlowerPotFrames.registerClient(FlowerPotContent.CARPENTERS_FLOWER_POT);
        }
        if (SafeContent.CARPENTERS_SAFE != null) {
            FrameModels.register(SafeContent.CARPENTERS_SAFE, SafeFrames.SAFE);
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
