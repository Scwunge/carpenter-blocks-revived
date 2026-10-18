package com.carpentersblocks.fabric.mixin;

import java.util.Map;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets axes strip log covers. NeoForge exposes this as {@code AxeItem.getAxeStrippingState}. */
@Mixin(AxeItem.class)
public interface AxeItemAccessor {

    @Accessor("STRIPPABLES")
    static Map<Block, Block> carpentersblocks$strippables() {
        throw new AssertionError();
    }
}
