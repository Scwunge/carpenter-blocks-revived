package com.carpentersblocks.integration.jade;

import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.entity.CarpentersTileEntity;
import com.carpentersblocks.integration.TooltipLines;

/**
 * Jade: shows the looked-at face's cover, dye, overlay and design, and a tile's look. Found by Jade's
 * {@code @WailaPlugin} scan on NeoForge and its {@code jade} entrypoint on Fabric, so it only loads with Jade.
 */
@WailaPlugin(CarpentersBlocks.MOD_ID)
public class JadePlugin implements IWailaPlugin {

    public static final ResourceLocation COVERS = CarpentersBlocks.id("covers");
    public static final ResourceLocation TILE = CarpentersBlocks.id("tile");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(Covers.INSTANCE, CoverableBlock.class);
        registration.registerEntityComponent(Tile.INSTANCE, CarpentersTileEntity.class);
    }

    private enum Covers implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (accessor.getBlockEntity() instanceof CoverableBlockEntity blockEntity) {
                TooltipLines.block(blockEntity, accessor.getSide()).forEach(tooltip::add);
            }
        }

        @Override
        public ResourceLocation getUid() {
            return COVERS;
        }
    }

    private enum Tile implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (accessor.getEntity() instanceof CarpentersTileEntity tile) {
                TooltipLines.tile(tile).forEach(tooltip::add);
            }
        }

        @Override
        public ResourceLocation getUid() {
            return TILE;
        }
    }
}
