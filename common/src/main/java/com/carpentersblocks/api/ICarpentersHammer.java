package com.carpentersblocks.api;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Implemented by items that act as a Carpenter's Hammer. Port of the original API interface. */
public interface ICarpentersHammer {

    /** Called after the hammer altered a block, typically to damage the item. */
    void onHammerUse(Level level, Player player, InteractionHand hand);

    /** Whether the hammer may be used right now. */
    boolean canUseHammer(Level level, Player player);
}
