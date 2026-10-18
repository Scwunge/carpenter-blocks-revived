package com.carpentersblocks.api;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Implemented by items that act as a Carpenter's Chisel. Port of the original API interface. */
public interface ICarpentersChisel {

    /** Called after the chisel altered a block, typically to damage the item. */
    void onChiselUse(Level level, Player player, InteractionHand hand);

    /** Whether the chisel may be used right now. */
    boolean canUseChisel(Level level, Player player);
}
