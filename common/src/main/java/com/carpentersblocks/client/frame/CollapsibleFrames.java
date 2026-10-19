package com.carpentersblocks.client.frame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.collapsible.CarpentersCollapsibleBlock;
import com.carpentersblocks.block.collapsible.CollapsibleGeometry;
import com.carpentersblocks.block.collapsible.CollapsibleGeometry.Depths;

/**
 * Frame of the Carpenter's Collapsible Block, as the original's {@code BlockHandlerCarpentersCollapsibleBlock}: the
 * sloped side is four triangles meeting at the middle ({@link CollapsibleGeometry#heights}), textured like the flat
 * top or bottom it stands in for; the upright sides follow the corners down to the flat side.
 */
public final class CollapsibleFrames {

    private static final ResourceLocation SOLID = CarpentersBlocks.id("block/general/solid");
    /** Sides lower than this together are left out, as the original's one-pixel step. */
    private static final double MIN_SIDE = 1.0 / 16;

    private CollapsibleFrames() {}

    public static final FrameBuilder COLLAPSIBLE = new FrameBuilder() {
        @Override
        @Nullable
        public Object renderKey(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            return CarpentersCollapsibleBlock.depths(level, pos);
        }

        @Override
        public void build(BlockState state, @Nullable Object key, FrameMesh mesh) {
            CollapsibleFrames.build(key instanceof Depths depths ? depths : Depths.FULL,
                    CarpentersCollapsibleBlock.isPositive(state), mesh);
        }
    };

    /** Built for a block standing on the floor, then turned upside down for one hanging from the ceiling. */
    static void build(Depths depths, boolean positive, FrameMesh mesh) {
        double[] h = CollapsibleGeometry.heights(depths);
        float nn = (float) h[0], np = (float) h[1], pn = (float) h[2], pp = (float) h[3], c = (float) h[4];
        Vector3f cornerNN = new Vector3f(0, nn, 0);
        Vector3f cornerNP = new Vector3f(0, np, 1);
        Vector3f cornerPN = new Vector3f(1, pn, 0);
        Vector3f cornerPP = new Vector3f(1, pp, 1);
        Vector3f center = new Vector3f(0.5F, c, 0.5F);

        Direction top = positive ? Direction.UP : Direction.DOWN;
        add(mesh, positive, top, center, cornerPN, cornerNN);
        add(mesh, positive, top, cornerPP, center, cornerNP);
        add(mesh, positive, top, center, cornerNN, cornerNP);
        add(mesh, positive, top, center, cornerPP, cornerPN);

        add(mesh, positive, null, new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(1, 0, 1),
                new Vector3f(0, 0, 1));
        if (nn + pn >= MIN_SIDE) {
            add(mesh, positive, null, new Vector3f(0, 0, 0), cornerNN, cornerPN, new Vector3f(1, 0, 0));
        }
        if (np + pp >= MIN_SIDE) {
            add(mesh, positive, null, new Vector3f(0, 0, 1), new Vector3f(1, 0, 1), cornerPP, cornerNP);
        }
        if (nn + np >= MIN_SIDE) {
            add(mesh, positive, null, new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), cornerNP, cornerNN);
        }
        if (pn + pp >= MIN_SIDE) {
            add(mesh, positive, null, new Vector3f(1, 0, 0), cornerPN, cornerPP, new Vector3f(1, 0, 1));
        }
    }

    /**
     * Adds a polygon given for a block on the floor, counter-clockwise from outside, flipped for a hanging block.
     * Corners at the bottom that coincide are merged, so a side with one corner down becomes a triangle.
     */
    private static void add(FrameMesh mesh, boolean positive, @Nullable Direction uvFace, Vector3f... vertices) {
        List<Vector3f> polygon = new ArrayList<>(vertices.length);
        for (Vector3f vertex : vertices) {
            Vector3f v = positive ? new Vector3f(vertex) : new Vector3f(vertex.x, 1 - vertex.y, vertex.z);
            if (polygon.isEmpty() || !polygon.get(polygon.size() - 1).equals(v, 1.0E-6F)) {
                polygon.add(v);
            }
        }
        if (polygon.size() > 1 && polygon.get(0).equals(polygon.get(polygon.size() - 1), 1.0E-6F)) {
            polygon.remove(polygon.size() - 1);
        }
        if (!positive) {
            Collections.reverse(polygon);
        }
        mesh.polygon(polygon, SOLID, null, uvFace);
    }
}
