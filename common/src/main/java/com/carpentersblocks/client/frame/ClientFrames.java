package com.carpentersblocks.client.frame;

import com.carpentersblocks.registry.BarrierContent;
import com.carpentersblocks.registry.RedstoneContent;

/** Registers every code-built frame. Both loaders' client entry points call this once during client setup. */
public final class ClientFrames {

    private ClientFrames() {}

    public static void registerAll() {
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
    }
}
