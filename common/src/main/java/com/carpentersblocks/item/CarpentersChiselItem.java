package com.carpentersblocks.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import com.carpentersblocks.api.ICarpentersChisel;
import com.carpentersblocks.block.coverable.CoverableBlock;

/**
 * The Carpenter's Chisel. Right-click a covered face to carve the next design into it, left-click for the previous
 * one, sneak + left-click to smooth it again. Port of {@code ItemCarpentersChisel}.
 */
public class CarpentersChiselItem extends Item implements ICarpentersChisel {

    public CarpentersChiselItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onChiselUse(Level level, Player player, InteractionHand hand) {
        CoverableBlock.damageItem(player, hand);
    }

    @Override
    public boolean canUseChisel(Level level, Player player) {
        return true;
    }
}
