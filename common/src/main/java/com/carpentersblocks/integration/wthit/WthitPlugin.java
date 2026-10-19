package com.carpentersblocks.integration.wthit;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;

import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.entity.CarpentersTileEntity;
import com.carpentersblocks.integration.TooltipLines;

/**
 * WTHIT: the same lines as the Jade plugin. Listed in {@code waila_plugins.json} as a client entrypoint, so it only
 * loads with WTHIT, which can switch it off in its plugin settings.
 */
public class WthitPlugin implements IWailaClientPlugin {

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(Covers.INSTANCE, CoverableBlock.class);
        registrar.body(Tile.INSTANCE, CarpentersTileEntity.class);
    }

    private enum Covers implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (accessor.getBlockEntity() instanceof CoverableBlockEntity blockEntity) {
                TooltipLines.block(blockEntity, accessor.getSide()).forEach(tooltip::addLine);
            }
        }
    }

    private enum Tile implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (accessor.getEntity() instanceof CarpentersTileEntity tile) {
                TooltipLines.tile(tile).forEach(tooltip::addLine);
            }
        }
    }
}
