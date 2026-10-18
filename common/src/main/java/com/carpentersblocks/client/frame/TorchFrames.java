package com.carpentersblocks.client.frame;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.barrier.Box;
import com.carpentersblocks.block.sided.LegacyVectors;
import com.carpentersblocks.block.sided.SidedBlock;
import com.carpentersblocks.block.torch.CarpentersTorchBlock;
import com.carpentersblocks.block.torch.TorchFlame;
import com.carpentersblocks.block.torch.TorchGeometry;
import com.carpentersblocks.block.torch.TorchType;

/**
 * Frame mesh of the Carpenter's Torch, from {@link TorchGeometry}. The handle and the lantern's body take the cover;
 * the flame and the lantern glass keep their own textures, as the original drew them.
 */
public final class TorchFrames {

    private static final ResourceLocation SOLID = CarpentersBlocks.id("block/general/solid");
    private static final ResourceLocation GLASS = CarpentersBlocks.id("block/torch/lantern_glass");
    /** The flame textures draw the head in pixels u 7-9, v 0-2. */
    private static final float[] HEAD_UV = { 7, 0, 9, 2 };

    private TorchFrames() {}

    private static ResourceLocation flameTexture(TorchFlame flame) {
        return CarpentersBlocks.id("block/torch/torch_head_" + flame.getSerializedName());
    }

    public static final FrameBuilder TORCH = new FrameBuilder() {
        @Override
        public void build(BlockState state, @Nullable Object key, FrameMesh mesh) {
            Direction facing = state.getValue(SidedBlock.FACING);
            if (state.getValue(CarpentersTorchBlock.TYPE) == TorchType.VANILLA) {
                Vector3f[] handle = TorchGeometry.handle(facing);
                for (int[] face : LegacyVectors.CUBOID_FACES) {
                    mesh.polygon(List.of(handle[face[0]], handle[face[1]], handle[face[2]], handle[face[3]]), SOLID,
                            null);
                }
                addFlame(mesh, TorchGeometry.flame(facing), state.getValue(CarpentersTorchBlock.FLAME));
            } else {
                for (Box box : TorchGeometry.lanternFrame(facing)) {
                    mesh.box((float) box.x1() / 16, (float) box.y1() / 16, (float) box.z1() / 16,
                            (float) box.x2() / 16, (float) box.y2() / 16, (float) box.z2() / 16, SOLID);
                }
                for (TorchGeometry.Pane pane : TorchGeometry.lanternGlass(facing)) {
                    FixedQuads.face(mesh, pane.box(), pane.faces(), GLASS, FixedQuads.WHITE);
                }
                // The lantern always shows a lit flame, as the original
                Box f = TorchGeometry.LANTERN_FLAME;
                Vector3f[] flame = LegacyVectors.stick((float) (f.x2() - f.x1()) / 32, (float) f.y1() / 16,
                        (float) f.y2() / 16);
                for (Vector3f v : flame) {
                    v.add(0.5F, 0, 0.5F);
                }
                addFlame(mesh, flame, TorchFlame.LIT);
            }
        }
    };

    private static void addFlame(FrameMesh mesh, Vector3f[] corners, TorchFlame flame) {
        for (BakedQuad quad : FixedQuads.stick(corners, flameTexture(flame), HEAD_UV, HEAD_UV, FixedQuads.WHITE)) {
            mesh.fixed(quad);
        }
    }
}
