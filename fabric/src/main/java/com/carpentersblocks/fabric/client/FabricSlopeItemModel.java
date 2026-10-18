package com.carpentersblocks.fabric.client;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import com.carpentersblocks.client.frame.SlopeItemModel;

/** Draws the slope item as the kind in its stack's component. */
public class FabricSlopeItemModel extends ForwardingBakedModel {

    private static final Direction[] CULL_FACES = { null, Direction.DOWN, Direction.UP, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST };

    private final SlopeItemModel slopes;

    public FabricSlopeItemModel(SlopeItemModel slopes) {
        this.wrapped = slopes;
        this.slopes = slopes;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        QuadEmitter emitter = context.getEmitter();
        RenderMaterial material = RendererAccess.INSTANCE.getRenderer().materialFinder().find();
        RandomSource random = randomSupplier.get();
        for (Direction cullFace : CULL_FACES) {
            for (BakedQuad quad : slopes.resolve(stack).getQuads(null, cullFace, random)) {
                emitter.fromVanilla(quad, material, cullFace);
                emitter.emit();
            }
        }
    }
}
