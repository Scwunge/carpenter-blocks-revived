package com.carpentersblocks.registry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.ladder.CarpentersLadderBlock;
import com.carpentersblocks.config.CBConfig;

/** Batch 6: the Carpenter's Ladder. */
public final class LadderContent {

    public static CarpentersLadderBlock CARPENTERS_LADDER;

    private LadderContent() {}

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        if (CBConfig.enableLadder) {
            // Hardness and sound of a vanilla ladder, as the original took its step sound
            CARPENTERS_LADDER = ModContent.block(registrar, "carpenters_ladder", new CarpentersLadderBlock(
                    CoverableBlock.baseProperties().strength(0.4F).sound(SoundType.LADDER)), true);
        }
    }
}
