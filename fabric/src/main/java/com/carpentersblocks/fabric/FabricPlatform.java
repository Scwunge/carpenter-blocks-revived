package com.carpentersblocks.fabric;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.fabric.mixin.AxeItemAccessor;
import com.carpentersblocks.platform.Platform;

/**
 * Fabric lookups: fire values from Fabric's flammability registry (which falls back to vanilla's). Fabric's names
 * follow Yarn: its burn chance is vanilla's ignite odds and its spread chance is vanilla's burn odds.
 */
public final class FabricPlatform implements Platform {

    @Override
    public int flammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return FlammableBlockRegistry.getDefaultInstance().get(state.getBlock()).getSpreadChance();
    }

    @Override
    public int fireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return FlammableBlockRegistry.getDefaultInstance().get(state.getBlock()).getBurnChance();
    }

    /** Reads vanilla's strippables, which Fabric's StrippableBlockRegistry adds to. */
    @Override
    public BlockState stripped(BlockState state) {
        Block stripped = AxeItemAccessor.carpentersblocks$strippables().get(state.getBlock());
        return stripped == null ? null : stripped.withPropertiesOf(state);
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction face) {
        return face == Direction.UP && state.is(level.dimensionType().infiniburn());
    }
}
