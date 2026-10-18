package com.carpentersblocks.client.frame;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.slope.CarpentersSlopeBlock;
import com.carpentersblocks.block.slope.Slope;
import com.carpentersblocks.block.slope.SlopeGeometry;

/**
 * Slope frames from {@link SlopeGeometry}. Uncovered, flat faces and wedge slopes show the full frame, oblique slopes
 * their own oblique textures, and prism slopes the quartered frame, as the original did.
 */
public final class SlopeFrames implements FrameBuilder {

    public static final SlopeFrames INSTANCE = new SlopeFrames();

    private static final ResourceLocation FULL_FRAME = CarpentersBlocks.id("block/general/full_frame");
    private static final ResourceLocation QUARTERED_FRAME = CarpentersBlocks.id("block/frame");
    private static final ResourceLocation OBLIQUE_POS = CarpentersBlocks.id("block/slope/oblique_pos");
    private static final ResourceLocation OBLIQUE_NEG = CarpentersBlocks.id("block/slope/oblique_neg");

    private SlopeFrames() {}

    @Override
    public void build(BlockState state, @Nullable Object key, FrameMesh mesh) {
        build(state.getValue(CarpentersSlopeBlock.SLOPE), mesh);
    }

    public static void build(Slope slope, FrameMesh mesh) {
        for (SlopeGeometry.Polygon polygon : SlopeGeometry.polygons(slope)) {
            List<Vector3f> vertices = new ArrayList<>();
            for (double[] v : polygon.vertices()) {
                vertices.add(new Vector3f((float) v[0], (float) v[1], (float) v[2]));
            }
            if (polygon.isAxisAligned()) {
                mesh.polygon(vertices, FULL_FRAME, null);
                continue;
            }
            double[] n = polygon.normal();
            // The horizontal direction the face leans toward; Z wins ties, as for the UV projection
            Direction side = Math.abs(n[0]) > Math.abs(n[2]) + 1.0E-6
                    ? (n[0] > 0 ? Direction.EAST : Direction.WEST)
                    : (n[2] > 0 ? Direction.SOUTH : Direction.NORTH);
            boolean positive = slope.type == Slope.Type.WEDGE_SIDE || slope.isPositive;
            mesh.polygon(vertices, sprite(slope, side), new FrameMesh.Sloped(positive, side,
                    slope.type == Slope.Type.WEDGE));
        }
    }

    private static ResourceLocation sprite(Slope slope, Direction side) {
        return switch (slope.getPrimaryType()) {
            case OBLIQUE_INT, OBLIQUE_EXT -> slope.isPositive ? OBLIQUE_POS : OBLIQUE_NEG;
            case PRISM -> QUARTERED_FRAME;
            // The prism wedge's main slope is a full wedge face; its ridge faces are prism faces
            case PRISM_WEDGE -> slope.facings.contains(side) ? FULL_FRAME : QUARTERED_FRAME;
            default -> FULL_FRAME;
        };
    }
}
