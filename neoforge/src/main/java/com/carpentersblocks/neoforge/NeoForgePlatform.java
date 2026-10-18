package com.carpentersblocks.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.platform.Platform;

/** NeoForge lookups: the block extension hooks, so covers from other mods report their own values. */
public final class NeoForgePlatform implements Platform {

    @Override
    public int flammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return state.getFlammability(level, pos, face);
    }

    @Override
    public int fireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return state.getFireSpreadSpeed(level, pos, face);
    }

    @Override
    public BlockState stripped(BlockState state) {
        return AxeItem.getAxeStrippingState(state);
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction face) {
        return state.isFireSource(level, pos, face);
    }
}
