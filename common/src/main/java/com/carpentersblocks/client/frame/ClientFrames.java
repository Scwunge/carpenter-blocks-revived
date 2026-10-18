package com.carpentersblocks.client.frame;

import com.carpentersblocks.registry.BarrierContent;

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
    }
}
