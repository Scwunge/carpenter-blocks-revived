package com.carpentersblocks.client.frame;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * Builds a block's frame geometry in code, for shapes JSON models can't express or that depend on the world. Both
 * loaders' cover models draw the mesh in place of the baked frame model, and {@code CoverQuads} re-textures it.
 */
public interface FrameBuilder {

    /**
     * World context the geometry depends on beyond the block state, read at chunk-build time (possibly off the main
     * thread, on a chunk-region view). The result is a cache key: immutable, with {@code equals} and {@code hashCode}
     * (a record). {@code null} means the state alone decides; it is also what items and world-less renders get.
     */
    @Nullable
    default Object renderKey(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return null;
    }

    /** Adds the frame's quads for {@code state} and the key from {@link #renderKey}. */
    void build(BlockState state, @Nullable Object key, FrameMesh mesh);
}
