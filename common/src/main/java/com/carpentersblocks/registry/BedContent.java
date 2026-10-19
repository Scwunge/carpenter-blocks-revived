package com.carpentersblocks.registry;

import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.carpentersblocks.block.bed.CarpentersBedBlock;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.config.CBConfig;

/** Batch 8: the Carpenter's Bed. */
public final class BedContent {

    public static CarpentersBedBlock CARPENTERS_BED;

    /**
     * Builds the bed block. NeoForge swaps in a subclass that answers its respawn hook, whose return type common code
     * can't name.
     */
    public static Function<BlockBehaviour.Properties, CarpentersBedBlock> factory = CarpentersBedBlock::new;

    private BedContent() {}

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        if (CBConfig.enableBed) {
            CARPENTERS_BED = ModContent.block(registrar, "carpenters_bed",
                    factory.apply(CoverableBlock.baseProperties().strength(0.4F)), true);
        }
    }
}
