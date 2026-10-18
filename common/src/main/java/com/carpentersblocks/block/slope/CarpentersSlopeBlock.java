package com.carpentersblocks.block.slope;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.config.CBConfig;

/**
 * Carpenter's Slope: 65 wedges, corners, obliques and prisms in five item kinds, joining up with neighbors when
 * placed. The hammer turns them (left-click) and changes their type (right-click). Port of the original
 * {@code BlockCarpentersSlope}; the slope ID moved from the tile entity into the block state.
 */
public class CarpentersSlopeBlock extends CoverableBlock {

    public static final MapCodec<CarpentersSlopeBlock> CODEC = simpleCodec(CarpentersSlopeBlock::new);

    public static final EnumProperty<Slope> SLOPE = EnumProperty.create("slope", Slope.class);
    /** True when every face is covered by an opaque block, so neighbors may cull against it. */
    public static final BooleanProperty SOLID = BooleanProperty.create("solid");

    /** Thickness of the occluding layer behind each full face. */
    private static final double FACE_LAYER = 1.0 / 16;

    private static int shapeSmoothness = -1;
    private static final Map<Slope, VoxelShape> SHAPES = new EnumMap<>(Slope.class);
    private static final Map<Slope, VoxelShape> OCCLUSION = new EnumMap<>(Slope.class);

    public CarpentersSlopeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(SLOPE, Slope.WEDGE_POS_N)
                .setValue(SOLID, false)
                .setValue(LIGHT, 0)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SLOPE, SOLID);
    }

    // Shapes

    /**
     * Collision and selection: columns on a grid of {@code slopeSmoothness} per side reaching the slope's top, as the
     * original's slices (prisms use half the smoothness, as the original did).
     */
    public static VoxelShape shape(Slope slope) {
        ensureShapes();
        return SHAPES.get(slope);
    }

    private static synchronized void ensureShapes() {
        int smoothness = Mth.clamp(CBConfig.slopeSmoothness, 1, 16);
        if (smoothness == shapeSmoothness) {
            return;
        }
        for (Slope slope : Slope.values()) {
            int n = slope.getPrimaryType() == Slope.Type.PRISM ? Math.max(1, smoothness / 2) : smoothness;
            SHAPES.put(slope, toShape(SlopeGeometry.boxes(slope, n, true)));

            // Occlusion stays inside the slope, plus a thin layer behind each full face so neighbors cull against it
            VoxelShape occlusion = toShape(SlopeGeometry.boxes(slope, smoothness, false));
            for (Direction side : Direction.values()) {
                if (slope.isFaceFull(side)) {
                    occlusion = Shapes.or(occlusion, faceLayer(side));
                }
            }
            OCCLUSION.put(slope, occlusion.optimize());
        }
        shapeSmoothness = smoothness;
    }

    private static VoxelShape toShape(List<double[]> boxes) {
        VoxelShape shape = Shapes.empty();
        for (double[] b : boxes) {
            shape = Shapes.or(shape, Shapes.box(b[0], b[1], b[2], b[3], b[4], b[5]));
        }
        return shape.optimize();
    }

    private static VoxelShape faceLayer(Direction side) {
        double min = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 - FACE_LAYER : 0;
        double max = min + FACE_LAYER;
        return switch (side.getAxis()) {
            case X -> Shapes.box(min, 0, 0, max, 1, 1);
            case Y -> Shapes.box(0, min, 0, 1, max, 1);
            case Z -> Shapes.box(0, 0, min, 1, 1, max);
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state.getValue(SLOPE));
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (!state.getValue(SOLID)) {
            return Shapes.empty();
        }
        ensureShapes();
        return OCCLUSION.get(state.getValue(SLOPE));
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return state.getValue(SOLID);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(SOLID);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    // Placement

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Vec3 hit = context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos));
        SlopeKind kind = SlopeItem.kind(context.getItemInHand());
        Slope slope = placement(kind, context.getClickedFace(),
                Mth.clamp(hit.x, 0, 1), Mth.clamp(hit.y, 0, 1), Mth.clamp(hit.z, 0, 1),
                context.getRotation(), context.getHorizontalDirection());

        // Sneaking skips joining up with neighbors, as in the original
        if (!context.isSecondaryUseActive()) {
            Level level = context.getLevel();
            if (kind == SlopeKind.WEDGE) {
                slope = SlopeTransform.transformWedge(slope, offset -> slopeAt(level, pos.offset(offset)));
            } else if (slope == Slope.PRISM_POS) {
                slope = SlopeTransform.transformPrism(slope, offset -> slopeAt(level, pos.offset(offset)));
            }
        }
        return defaultBlockState().setValue(SLOPE, slope).setValue(WATERLOGGED, placedInWater(context));
    }

    /**
     * The slope placed for an item kind, as the original's {@code onBlockPlacedBy}: upward when clicking a block's top
     * or the lower half of its side; corners from the player's yaw; wedges from where the face was clicked.
     *
     * @param face   the clicked face of the block clicked on
     * @param facing the horizontal direction the player looks
     */
    public static Slope placement(SlopeKind kind, Direction face, double hitX, double hitY, double hitZ, float yaw,
            Direction facing) {
        int side = face.get3DDataValue();
        boolean positive = side > 1 && hitY < 0.5 || side == 1;
        Corner corner = Corner.fromYaw(yaw);
        return switch (kind) {
            case WEDGE -> wedgeOrientation(facing, side, hitX, hitY, hitZ);
            case OBLIQUE_INTERIOR -> switch (corner) {
                case SE -> positive ? Slope.OBL_INT_POS_SE : Slope.OBL_INT_NEG_SE;
                case NE -> positive ? Slope.OBL_INT_POS_NE : Slope.OBL_INT_NEG_NE;
                case NW -> positive ? Slope.OBL_INT_POS_NW : Slope.OBL_INT_NEG_NW;
                case SW -> positive ? Slope.OBL_INT_POS_SW : Slope.OBL_INT_NEG_SW;
            };
            case OBLIQUE_EXTERIOR -> switch (corner) {
                case SE -> positive ? Slope.OBL_EXT_POS_SE : Slope.OBL_EXT_NEG_SE;
                case NE -> positive ? Slope.OBL_EXT_POS_NE : Slope.OBL_EXT_NEG_NE;
                case NW -> positive ? Slope.OBL_EXT_POS_NW : Slope.OBL_EXT_NEG_NW;
                case SW -> positive ? Slope.OBL_EXT_POS_SW : Slope.OBL_EXT_NEG_SW;
            };
            case PRISM -> positive ? Slope.PRISM_POS : Slope.PRISM_NEG;
            case PRISM_WEDGE -> switch (facing) {
                case SOUTH -> Slope.PRISM_WEDGE_POS_N;
                case WEST -> Slope.PRISM_WEDGE_POS_E;
                case EAST -> Slope.PRISM_WEDGE_POS_W;
                default -> Slope.PRISM_WEDGE_POS_S;
            };
        };
    }

    private enum Corner {
        SE, NE, NW, SW;

        static Corner fromYaw(float yaw) {
            return switch (Mth.floor(yaw * 4.0F / 360.0F) & 3) {
                case 0 -> NE;
                case 1 -> SE;
                case 2 -> SW;
                default -> NW;
            };
        }
    }

    /** The original's {@code getWedgeOrientation}: side wedges near a side face's edges, else up or down wedges. */
    private static Slope wedgeOrientation(Direction facing, int side, double hitX, double hitY, double hitZ) {
        switch (side) {
            case 2 -> hitX = 1.0 - hitX;
            case 4 -> hitX = hitZ;
            case 5 -> hitX = 1.0 - hitZ;
            default -> {}
        }

        int slopeID;
        if (side > 1) {
            if (hitY > 0.5 && hitX > 1.0 - hitY && hitX < hitY) {
                slopeID = side + 2;
            } else if (hitY < 0.5 && hitX < 1.0 - hitY && hitX > hitY) {
                slopeID = side + 6;
            } else if (hitX < 0.2) {
                slopeID = side == 2 ? 1 : side == 3 ? 0 : side == 4 ? 3 : 2;
            } else if (hitX > 0.8) {
                slopeID = side == 2 ? 2 : side == 3 ? 3 : side == 4 ? 1 : 0;
            } else if (hitY > 0.5) {
                slopeID = side + 2;
            } else {
                slopeID = side + 6;
            }
        } else {
            slopeID = side + 12;
        }

        if (slopeID <= 11) {
            return Slope.byId(slopeID);
        }
        boolean negative = slopeID == 12;
        return switch (facing) {
            case NORTH -> negative ? Slope.WEDGE_NEG_S : Slope.WEDGE_POS_S;
            case SOUTH -> negative ? Slope.WEDGE_NEG_N : Slope.WEDGE_POS_N;
            case WEST -> negative ? Slope.WEDGE_NEG_E : Slope.WEDGE_POS_E;
            case EAST -> negative ? Slope.WEDGE_NEG_W : Slope.WEDGE_POS_W;
            default -> Slope.WEDGE_SE;
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide || placer != null && placer.isShiftKeyDown()) {
            return;
        }
        SlopeKind kind = SlopeItem.kind(stack);
        Slope slope = state.getValue(SLOPE);
        Map<Direction, Slope> changes;
        if (kind == SlopeKind.WEDGE) {
            changes = SlopeTransform.transformAdjacentWedges(slope, offset -> slopeAt(level, pos.offset(offset)));
        } else if (kind == SlopeKind.PRISM && slope.isPositive) {
            changes = SlopeTransform.transformAdjacentPrisms(offset -> slopeAt(level, pos.offset(offset)));
        } else {
            return;
        }
        changes.forEach((dir, changed) -> {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.getBlock() instanceof CarpentersSlopeBlock && neighborState.getValue(SLOPE) != changed) {
                level.setBlock(neighbor, neighborState.setValue(SLOPE, changed), Block.UPDATE_ALL);
            }
        });
    }

    /** The slope of the Carpenter's Slope at {@code pos}, or {@code null} if there is none. */
    @Nullable
    private static Slope slopeAt(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof CarpentersSlopeBlock ? state.getValue(SLOPE) : null;
    }

    // Hammer

    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        return setSlope(level, pos, state, state.getValue(SLOPE).rotate(Rotation.CLOCKWISE_90));
    }

    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        return setSlope(level, pos, state, state.getValue(SLOPE).nextType());
    }

    private static boolean setSlope(Level level, BlockPos pos, BlockState state, Slope slope) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(SLOPE, slope), Block.UPDATE_ALL);
        }
        return true;
    }

    @Override
    protected double toolDamageChance() {
        return CBConfig.itemHammerDamageChanceFromSlopes;
    }

    // Covers

    /** Side covers go only on full faces, as the original's {@code canCoverSide}. */
    @Override
    protected boolean canCoverSide(CoverableBlockEntity be, int slot) {
        return slot == CoverData.BASE
                || be.getBlockState().getValue(SLOPE).isFaceFull(Direction.from3DDataValue(slot));
    }

    @Override
    public BlockState stateForCovers(BlockState state, CoverData data) {
        boolean solid = true;
        for (Direction face : Direction.values()) {
            BlockState cover = data.coverFor(face);
            if (cover == null || !cover.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
                solid = false;
                break;
            }
        }
        return super.stateForCovers(state, data).setValue(SOLID, solid);
    }

    // Item

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return SlopeItem.stack(this, SlopeKind.of(state.getValue(SLOPE)));
    }

    // Structures

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(SLOPE, state.getValue(SLOPE).rotate(rotation));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(SLOPE, state.getValue(SLOPE).mirror(mirror));
    }
}
