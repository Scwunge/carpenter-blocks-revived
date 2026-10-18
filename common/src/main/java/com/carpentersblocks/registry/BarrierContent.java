package com.carpentersblocks.registry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.carpentersblocks.block.barrier.CarpentersBarrierBlock;
import com.carpentersblocks.block.barrier.CarpentersGateBlock;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.config.CBConfig;

/** Batch 4: the Carpenter's Barrier and Gate. */
public final class BarrierContent {

    public static CarpentersBarrierBlock CARPENTERS_BARRIER;
    public static CarpentersGateBlock CARPENTERS_GATE;

    private BarrierContent() {}

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        if (CBConfig.enableBarrier) {
            CARPENTERS_BARRIER = ModContent.block(registrar, "carpenters_barrier",
                    new CarpentersBarrierBlock(CoverableBlock.baseProperties()), true);
        }
        if (CBConfig.enableGate) {
            CARPENTERS_GATE = ModContent.block(registrar, "carpenters_gate",
                    new CarpentersGateBlock(CoverableBlock.baseProperties()), true);
        }
    }
}
