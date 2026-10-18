package com.carpentersblocks.fabric.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.blockentity.CarpentersBlockEntity;
import com.carpentersblocks.blockentity.CoverData;
import com.carpentersblocks.client.CoverQuads;

/** Fabric Renderer API model that draws the frame re-textured with its covers. */
public class FabricCoverModel extends ForwardingBakedModel {

    private static final Direction[] CULL_FACES = { null, Direction.DOWN, Direction.UP, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST };

    private final Map<BlendMode, RenderMaterial> materials = new ConcurrentHashMap<>();

    public FabricCoverModel(BakedModel frame) {
        this.wrapped = frame;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos,
            Supplier<RandomSource> randomSupplier, RenderContext context) {
        CoverData data = level.getBlockEntity(pos) instanceof CarpentersBlockEntity blockEntity
                ? blockEntity.getCoverData()
                : null;
        RandomSource random = randomSupplier.get();
        QuadEmitter emitter = context.getEmitter();

        for (Direction cullFace : CULL_FACES) {
            for (CoverQuads.Part part : CoverQuads.build(wrapped, state, cullFace, data, random,
                    ItemBlockRenderTypes::getChunkRenderType)) {
                emitter.fromVanilla(part.quad(), material(BlendMode.fromRenderLayer(part.layer())), cullFace);
                emitter.emit();
            }
        }
    }

    private RenderMaterial material(BlendMode blendMode) {
        return materials.computeIfAbsent(blendMode, mode -> {
            Renderer renderer = RendererAccess.INSTANCE.getRenderer();
            return renderer.materialFinder().blendMode(mode).find();
        });
    }
}
