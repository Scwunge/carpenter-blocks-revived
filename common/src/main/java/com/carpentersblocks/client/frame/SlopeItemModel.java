package com.carpentersblocks.client.frame;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.slope.SlopeItem;
import com.carpentersblocks.block.slope.SlopeKind;

/**
 * The slope item's model: the baked JSON model supplies transforms and particle, and each kind draws its display
 * slope's bare frame, chosen from the stack's {@code slope_kind} component. Vanilla offers no way to subclass
 * {@link ItemOverrides} from common code, so each loader picks the kind's model through {@link #resolve}.
 */
public final class SlopeItemModel implements BakedModel {

    private final BakedModel base;
    @Nullable
    private final FrameMesh mesh;
    private final Map<SlopeKind, SlopeItemModel> kinds;

    public SlopeItemModel(BakedModel base) {
        this(base, null, new EnumMap<>(SlopeKind.class));
    }

    private SlopeItemModel(BakedModel base, @Nullable FrameMesh mesh, Map<SlopeKind, SlopeItemModel> kinds) {
        this.base = base;
        this.mesh = mesh;
        this.kinds = kinds;
    }

    /** The model for a stack's kind. */
    public BakedModel resolve(ItemStack stack) {
        return kind(SlopeItem.kind(stack));
    }

    private synchronized SlopeItemModel kind(SlopeKind kind) {
        return kinds.computeIfAbsent(kind, k -> {
            FrameMesh kindMesh = new FrameMesh();
            SlopeFrames.build(k.display, kindMesh);
            return new SlopeItemModel(base, kindMesh, kinds);
        });
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        FrameMesh shown = mesh != null ? mesh : kind(SlopeKind.WEDGE).mesh;
        return shown.quads(side).stream().map(FrameMesh.Quad::quad).toList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return base.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return base.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
