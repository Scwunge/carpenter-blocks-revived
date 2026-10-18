package com.carpentersblocks.registry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.stairs.CarpentersStairsBlock;
import com.carpentersblocks.config.CBConfig;

/** Batch 3: the Carpenter's Stairs. */
public final class StairsContent {

    public static CarpentersStairsBlock CARPENTERS_STAIRS;

    private StairsContent() {}

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        if (CBConfig.enableStairs) {
            CARPENTERS_STAIRS = ModContent.block(registrar, "carpenters_stairs",
                    new CarpentersStairsBlock(CoverableBlock.baseProperties()), true);
        }
    }
}
