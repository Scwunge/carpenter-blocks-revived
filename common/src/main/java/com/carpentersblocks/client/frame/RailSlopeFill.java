package com.carpentersblocks.client.frame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.carpentersblocks.block.slope.RailSlopes;
import com.carpentersblocks.block.slope.Slope;

/**
 * Meshes of the rail slope fill ({@link RailSlopes}). Each cover model owns one instance, so the meshes are rebuilt
 * with the model after a resource reload.
 */
public final class RailSlopeFill {

    private final Map<Slope, FrameMesh> meshes = new ConcurrentHashMap<>();

    /** The unculled frame quads of the fill, one block up. */
    public FrameMesh mesh(Slope slope) {
        return meshes.computeIfAbsent(slope, s -> {
            FrameMesh mesh = FrameMesh.raised();
            SlopeFrames.build(s, mesh);
            return mesh;
        });
    }
}
