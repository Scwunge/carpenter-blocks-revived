package com.carpentersblocks.registry;

import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.torch.CarpentersTorchBlock;
import com.carpentersblocks.config.CBConfig;

/** Batch 6: the Carpenter's Torch. */
public final class TorchContent {

    public static CarpentersTorchBlock CARPENTERS_TORCH;

    private TorchContent() {}

    public static void registerBlocks(BiConsumer<ResourceLocation, Block> registrar) {
        if (CBConfig.enableTorch) {
            CARPENTERS_TORCH = ModContent.block(registrar, "carpenters_torch", new CarpentersTorchBlock(
                    CoverableBlock.baseProperties().strength(0.2F).lightLevel(CarpentersTorchBlock::lightLevel)), true);
        }
    }
}
