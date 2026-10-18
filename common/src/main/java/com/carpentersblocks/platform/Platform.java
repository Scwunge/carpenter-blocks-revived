package com.carpentersblocks.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block properties whose lookup differs between loaders. Each loader calls {@link #set} before registration.
 */
public interface Platform {

    /** Set by the loader entry point. */
    Platform[] HOLDER = new Platform[1];

    static Platform get() {
        return HOLDER[0];
    }

    static void set(Platform platform) {
        HOLDER[0] = platform;
    }

    /** How readily the block burns away (vanilla's burn odds). */
    int flammability(BlockState state, BlockGetter level, BlockPos pos, Direction face);

    /** How readily the block catches fire from nearby fire (vanilla's ignite odds). */
    int fireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face);

    /** Whether fire on top of the block burns forever. */
    boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction face);
}
