package com.carpentersblocks.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import com.carpentersblocks.api.ICarpentersHammer;
import com.carpentersblocks.block.coverable.CoverableBlock;

/**
 * The Carpenter's Hammer. Each block decides what the hammer does to it; on any block, sneak + left-click takes off
 * the last thing applied to the clicked face. Port of {@code ItemCarpentersHammer}.
 */
public class CarpentersHammerItem extends Item implements ICarpentersHammer {

    public CarpentersHammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onHammerUse(Level level, Player player, InteractionHand hand) {
        CoverableBlock.damageItem(player, hand);
    }

    @Override
    public boolean canUseHammer(Level level, Player player) {
        return true;
    }
}
