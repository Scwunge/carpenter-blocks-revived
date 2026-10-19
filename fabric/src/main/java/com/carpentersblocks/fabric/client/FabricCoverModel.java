package com.carpentersblocks.fabric.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.slope.RailSlopes;
import com.carpentersblocks.block.slope.Slope;
import com.carpentersblocks.client.CoverQuads;
import com.carpentersblocks.client.frame.FrameBuilder;
import com.carpentersblocks.client.frame.FrameMesh;
import com.carpentersblocks.client.frame.FrameModels;
import com.carpentersblocks.client.frame.RailSlopeFill;

/** Fabric Renderer API model that draws the frame re-textured with its covers. */
public class FabricCoverModel extends ForwardingBakedModel {

    private static final Direction[] CULL_FACES = { null, Direction.DOWN, Direction.UP, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST };

    private final Map<BlendMode, RenderMaterial> materials = new ConcurrentHashMap<>();
    private final Map<BlendMode, RenderMaterial> emissiveMaterials = new ConcurrentHashMap<>();
    private final FrameModels.Cache frames = new FrameModels.Cache();
    private final RailSlopeFill railFill = new RailSlopeFill();

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
        FrameBuilder builder = FrameModels.builder(state.getBlock());
        BlockPos coverPos = builder == null ? pos : builder.coverSource(level, pos, state);
        CoverData data = level.getBlockEntity(coverPos) instanceof CoverableBlockEntity blockEntity
                ? blockEntity.getCoverData()
                : null;
        RandomSource random = randomSupplier.get();
        QuadEmitter emitter = context.getEmitter();

        FrameMesh mesh = builder == null ? null : frames.mesh(builder, state, builder.renderKey(level, pos, state));

        for (Direction cullFace : CULL_FACES) {
            List<CoverQuads.Part> parts = mesh == null
                    ? CoverQuads.build(wrapped, state, cullFace, data, random, ItemBlockRenderTypes::getChunkRenderType)
                    : CoverQuads.build(mesh.quads(cullFace), cullFace, data, random,
                            ItemBlockRenderTypes::getChunkRenderType);
            for (CoverQuads.Part part : parts) {
                emitter.fromVanilla(part.quad(), material(part), cullFace);
                emitter.emit();
            }
        }

        Slope fill = RailSlopes.slopeAbove(level, pos, state);
        if (fill != null) {
            for (CoverQuads.Part part : CoverQuads.build(railFill.mesh(fill).quads(null), null, data, random,
                    ItemBlockRenderTypes::getChunkRenderType)) {
                emitter.fromVanilla(part.quad(), material(part), null);
                emitter.emit();
            }
        }
    }

    private RenderMaterial material(CoverQuads.Part part) {
        BlendMode blendMode = BlendMode.fromRenderLayer(part.layer());
        if (part.emissive()) {
            return emissiveMaterials.computeIfAbsent(blendMode, mode -> RendererAccess.INSTANCE.getRenderer()
                    .materialFinder().blendMode(mode).emissive(true).disableDiffuse(true)
                    .ambientOcclusion(TriState.FALSE).find());
        }
        return materials.computeIfAbsent(blendMode, mode -> {
            Renderer renderer = RendererAccess.INSTANCE.getRenderer();
            return renderer.materialFinder().blendMode(mode).find();
        });
    }
}
