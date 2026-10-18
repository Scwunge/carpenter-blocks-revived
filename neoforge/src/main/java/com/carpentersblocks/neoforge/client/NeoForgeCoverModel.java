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

import com.carpentersblocks.blockentity.CarpentersBlockEntity;
import com.carpentersblocks.blockentity.CoverData;
import com.carpentersblocks.client.CoverQuads;

/** NeoForge model that draws the frame re-textured with its covers. */
public class NeoForgeCoverModel extends BakedModelWrapper<BakedModel> {

    private static final ModelProperty<CoverData> COVERS = new ModelProperty<>();

    public NeoForgeCoverModel(BakedModel frame) {
        super(frame);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        if (level.getBlockEntity(pos) instanceof CarpentersBlockEntity blockEntity) {
            return modelData.derive().with(COVERS, blockEntity.getCoverData()).build();
        }
        return modelData;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
            ModelData data, @Nullable RenderType renderType) {
        if (state == null) {
            return super.getQuads(null, side, random, data, renderType);
        }
        List<BakedQuad> quads = new ArrayList<>();
        for (CoverQuads.Part part : CoverQuads.build(originalModel, state, side, data.get(COVERS), random,
                NeoForgeCoverModel::layerOf)) {
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
        for (Direction face : Direction.values()) {
            BlockState cover = covers == null ? null : covers.coverFor(face);
            layers.add(cover == null ? CoverQuads.FRAME_LAYER : layerOf(cover));
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
