package com.carpentersblocks.client.frame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/** The blocks whose frame is built in code, and a mesh cache for each baked cover model. */
public final class FrameModels {

    private static final Map<Block, FrameBuilder> BUILDERS = new ConcurrentHashMap<>();

    private FrameModels() {}

    /** Registers a code-built frame; called from {@link ClientFrames#registerAll()} during client setup. */
    public static void register(Block block, FrameBuilder builder) {
        BUILDERS.put(block, builder);
    }

    @Nullable
    public static FrameBuilder builder(Block block) {
        return BUILDERS.get(block);
    }

    /**
     * Meshes built so far, keyed by state and render key. Each cover model owns one, so a resource reload (which
     * bakes new models and may move sprites) starts from an empty cache.
     */
    public static final class Cache {

        private record Key(BlockState state, @Nullable Object renderKey) {}

        private final Map<Key, FrameMesh> meshes = new ConcurrentHashMap<>();

        public FrameMesh mesh(FrameBuilder builder, BlockState state, @Nullable Object renderKey) {
            return meshes.computeIfAbsent(new Key(state, renderKey), key -> {
                FrameMesh mesh = new FrameMesh();
                builder.build(state, renderKey, mesh);
                return mesh;
            });
        }
    }
}
