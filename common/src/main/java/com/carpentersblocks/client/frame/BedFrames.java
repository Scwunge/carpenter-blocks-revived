package com.carpentersblocks.client.frame;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.bed.BedGeometry;
import com.carpentersblocks.block.bed.CarpentersBedBlock;
import com.carpentersblocks.block.design.DesignList;

/**
 * Frame mesh of the Carpenter's Bed, from {@link BedGeometry}. The legs, headboard and support board take the cover;
 * the mattress is white wool, the pillow its own texture, and the blanket wool of its dye color, or the bed's design
 * cut from the design's template, as the original drew them.
 */
public final class BedFrames {

    private static final ResourceLocation SOLID = CarpentersBlocks.id("block/general/solid");
    private static final ResourceLocation MATTRESS = ResourceLocation.withDefaultNamespace("block/white_wool");
    private static final ResourceLocation PILLOW = CarpentersBlocks.id("block/bed/bed_pillow");
    /** Design templates are 48 by 64 pixels. */
    private static final float TEMPLATE_WIDTH = 48, TEMPLATE_HEIGHT = 64;

    private BedFrames() {}

    public static final FrameBuilder BED = new FrameBuilder() {
        @Override
        @Nullable
        public Object renderKey(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            return CarpentersBedBlock.look(level, pos, state);
        }

        @Override
        public void build(BlockState state, @Nullable Object key, FrameMesh mesh) {
            CarpentersBedBlock.Look look = key instanceof CarpentersBedBlock.Look l ? l
                    : new CarpentersBedBlock.Look(BedGeometry.Joins.NONE, "", null);
            Direction facing = state.getValue(CarpentersBedBlock.FACING);
            boolean head = CarpentersBedBlock.isHead(state);
            boolean occupied = state.getValue(CarpentersBedBlock.OCCUPIED);

            for (Box box : BedGeometry.frame(head, look.joins())) {
                Box b = box.facing(facing);
                mesh.box((float) b.x1() / 16, (float) b.y1() / 16, (float) b.z1() / 16, (float) b.x2() / 16,
                        (float) b.y2() / 16, (float) b.z2() / 16, SOLID);
            }

            boolean design = !look.design().isEmpty();
            TextureAtlasSprite template = design ? FixedQuads.sprite(DesignList.BED.texture(look.design())) : null;
            for (BedGeometry.FabricBox fabric : BedGeometry.fabric(head, look.joins(), occupied)) {
                boolean fromTemplate = design && fabric.fabric() != BedGeometry.Fabric.MATTRESS;
                if (fromTemplate) {
                    addTemplateBox(mesh, fabric, head, occupied, facing, template);
                } else if (fabric.fabric() == BedGeometry.Fabric.PILLOW) {
                    addPillow(mesh, fabric.box(), facing);
                } else {
                    TextureAtlasSprite sprite = FixedQuads.sprite(texture(fabric.fabric(), look.blanket()));
                    Box b = fabric.box().facing(facing);
                    for (Direction face : Direction.values()) {
                        mesh.fixed(FixedQuads.face(b, face, sprite, FixedQuads.WHITE));
                    }
                }
            }
        }
    };

    private static ResourceLocation texture(BedGeometry.Fabric fabric, @Nullable DyeColor blanket) {
        return switch (fabric) {
            case MATTRESS -> MATTRESS;
            case PILLOW -> PILLOW;
            default -> ResourceLocation.withDefaultNamespace(
                    "block/" + (blanket == null ? DyeColor.WHITE : blanket).getName() + "_wool");
        };
    }

    /**
     * The pillow, its UVs worked out facing south and then turned, so the texture turns with the bed. The texture
     * fills rows 3 to 8, where the original's pillow (z 3 to 9, head to the north) took its top from; the top here
     * takes the bottom's UVs, which mirror z to match.
     */
    private static void addPillow(FrameMesh mesh, Box box, Direction facing) {
        TextureAtlasSprite sprite = FixedQuads.sprite(PILLOW);
        for (Direction face : Direction.values()) {
            Vector3f[] corners = FixedQuads.faceCorners(box, face);
            float[][] uv = new float[4][];
            for (int i = 0; i < 4; ++i) {
                uv[i] = FrameMesh.project(corners[i], face == Direction.UP ? Direction.DOWN : face);
                turn(corners[i], facing);
            }
            mesh.fixed(FixedQuads.bake(corners, uv, sprite, FixedQuads.WHITE));
        }
    }

    /** A fabric box textured from the design template, worked out facing south and then turned. */
    private static void addTemplateBox(FrameMesh mesh, BedGeometry.FabricBox fabric, boolean head, boolean occupied,
            Direction facing, TextureAtlasSprite template) {
        for (Direction face : Direction.values()) {
            Vector3f[] corners = FixedQuads.faceCorners(fabric.box(), face);
            float[][] uv = new float[4][];
            for (int i = 0; i < 4; ++i) {
                Vector3f p = corners[i];
                double[] t = BedGeometry.templateUv(fabric.fabric(), head, occupied, p.x * 16, p.y * 16, p.z * 16,
                        face);
                uv[i] = new float[] { (float) t[0] / TEMPLATE_WIDTH, (float) t[1] / TEMPLATE_HEIGHT };
                turn(p, facing);
            }
            mesh.fixed(FixedQuads.bake(corners, uv, template, FixedQuads.WHITE));
        }
    }

    /** Turns a point (block units) of a south-facing drawing to {@code facing}, as {@link Box#facing}. */
    private static void turn(Vector3f p, Direction facing) {
        float x = p.x, z = p.z;
        switch (facing) {
            case NORTH -> p.set(1 - x, p.y, 1 - z);
            case EAST -> p.set(z, p.y, 1 - x);
            case WEST -> p.set(1 - z, p.y, x);
            default -> {}
        }
    }
}
