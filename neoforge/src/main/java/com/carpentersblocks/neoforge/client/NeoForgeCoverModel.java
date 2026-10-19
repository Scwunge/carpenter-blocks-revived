package com.carpentersblocks.neoforge.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.slope.RailSlopes;
import com.carpentersblocks.block.slope.Slope;
import com.carpentersblocks.client.CoverQuads;
import com.carpentersblocks.client.frame.FrameBuilder;
import com.carpentersblocks.client.frame.FrameMesh;
import com.carpentersblocks.client.frame.FrameModels;
import com.carpentersblocks.client.frame.RailSlopeFill;

/** NeoForge model that draws the frame re-textured with its covers. */
public class NeoForgeCoverModel extends BakedModelWrapper<BakedModel> {

    private static final ModelProperty<CoverData> COVERS = new ModelProperty<>();
    private static final ModelProperty<Object> FRAME_KEY = new ModelProperty<>();
    private static final ModelProperty<Slope> RAIL_FILL = new ModelProperty<>();

    private final FrameModels.Cache frames = new FrameModels.Cache();
    private final RailSlopeFill railFill = new RailSlopeFill();

    public NeoForgeCoverModel(BakedModel frame) {
        super(frame);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        ModelData.Builder data = modelData.derive();
        FrameBuilder builder = FrameModels.builder(state.getBlock());
        BlockPos coverPos = builder == null ? pos : builder.coverSource(level, pos, state);
        if (level.getBlockEntity(coverPos) instanceof CoverableBlockEntity blockEntity) {
            data.with(COVERS, blockEntity.getCoverData());
        }
        if (builder != null) {
            data.with(FRAME_KEY, builder.renderKey(level, pos, state));
        }
        data.with(RAIL_FILL, RailSlopes.slopeAbove(level, pos, state));
        return data.build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
            ModelData data, @Nullable RenderType renderType) {
        if (state == null) {
            return super.getQuads(null, side, random, data, renderType);
        }
        FrameBuilder builder = FrameModels.builder(state.getBlock());
        List<CoverQuads.Part> parts = builder == null
                ? CoverQuads.build(originalModel, state, side, data.get(COVERS), random, NeoForgeCoverModel::layerOf)
                : CoverQuads.build(frames.mesh(builder, state, data.get(FRAME_KEY)).quads(side), side,
                        data.get(COVERS), random, NeoForgeCoverModel::layerOf);
        Slope fill = data.get(RAIL_FILL);
        if (side == null && fill != null) {
            parts = new ArrayList<>(parts);
            parts.addAll(CoverQuads.build(railFill.mesh(fill).quads(null), null, data.get(COVERS), random,
                    NeoForgeCoverModel::layerOf));
        }
        List<BakedQuad> quads = new ArrayList<>();
        for (CoverQuads.Part part : parts) {
            if (renderType == null || part.layer() == renderType) {
                quads.add(part.quad());
            }
        }
        return quads;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        CoverData covers = data.get(COVERS);
        Set<RenderType> layers = new HashSet<>();
        // Fixed quads (blankets, handles, inlays, flames, potted plants) keep the frame's layer under any cover
        FrameBuilder builder = FrameModels.builder(state.getBlock());
        if (builder != null) {
            FrameMesh mesh = frames.mesh(builder, state, data.get(FRAME_KEY));
            if (mesh.hasFixed()) {
                layers.add(CoverQuads.FRAME_LAYER);
            }
            if (mesh.hasTranslucent()) {
                layers.add(CoverQuads.PANE_LAYER);
            }
        }
        for (Direction face : Direction.values()) {
            if (covers == null) {
                layers.add(CoverQuads.FRAME_LAYER);
                continue;
            }
            int slot = covers.slotFor(face);
            BlockState cover = covers.cover(slot);
            layers.add(cover == null ? CoverQuads.FRAME_LAYER : layerOf(cover));
            if (covers.overlay(slot) != null) {
                layers.add(CoverQuads.OVERLAY_LAYER);
            }
            if (cover != null && covers.design(slot) != null) {
                layers.add(CoverQuads.DESIGN_LAYER);
            }
        }
        return ChunkRenderTypeSet.of(layers);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        CoverData covers = data.get(COVERS);
        BlockState base = covers == null ? null : covers.cover(CoverData.BASE);
        if (base != null) {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(base).getParticleIcon(ModelData.EMPTY);
        }
        return super.getParticleIcon(data);
    }

    /** First chunk layer the cover's own model renders in. */
    private static RenderType layerOf(BlockState cover) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(cover);
        ChunkRenderTypeSet types = model.getRenderTypes(cover, RandomSource.create(42L), ModelData.EMPTY);
        for (RenderType type : types) {
            return type;
        }
        return RenderType.solid();
    }
}
