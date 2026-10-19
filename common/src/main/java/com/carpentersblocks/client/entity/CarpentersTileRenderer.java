package com.carpentersblocks.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.design.DesignList;
import com.carpentersblocks.client.frame.FixedQuads;
import com.carpentersblocks.entity.CarpentersTileEntity;

/**
 * Draws a Carpenter's Tile as a block-textured slab: the design (or the blank tile) on every face, in the dye color,
 * turned by the tile's rotation. Port of {@code RenderCarpentersTile}.
 */
public class CarpentersTileRenderer extends EntityRenderer<CarpentersTileEntity> {

    public static final ResourceLocation BLANK = CarpentersBlocks.id("block/tile/blank");

    public CarpentersTileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CarpentersTileEntity tile, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight) {
        BlockPos pos = tile.getPos();
        AABB bounds = CarpentersTileEntity.box(pos, tile.getDirection()).move(-pos.getX(), -pos.getY(), -pos.getZ());
        Box box = new Box(bounds.minX * 16, bounds.minY * 16, bounds.minZ * 16, bounds.maxX * 16, bounds.maxY * 16,
                bounds.maxZ * 16);
        String design = tile.getDesign();
        TextureAtlasSprite sprite = FixedQuads.sprite(design.isEmpty() ? BLANK : DesignList.TILE.texture(design));
        int color = tile.getDye().getFireworkColor();
        float r = (color >> 16 & 0xFF) / 255F;
        float g = (color >> 8 & 0xFF) / 255F;
        float b = (color & 0xFF) / 255F;

        poseStack.pushPose();
        poseStack.translate(pos.getX() - tile.getX(), pos.getY() - tile.getY(), pos.getZ() - tile.getZ());
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        for (Direction face : Direction.values()) {
            consumer.putBulkData(poseStack.last(), FixedQuads.face(box, face, sprite, FixedQuads.WHITE,
                    tile.getRotation()), r, g, b, 1.0F, packedLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        super.render(tile, yaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CarpentersTileEntity tile) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
