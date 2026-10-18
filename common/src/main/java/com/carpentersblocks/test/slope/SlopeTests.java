package com.carpentersblocks.test.slope;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.slope.CarpentersSlopeBlock;
import com.carpentersblocks.block.slope.RailSlopes;
import com.carpentersblocks.block.slope.Slope;
import com.carpentersblocks.block.slope.SlopeItem;
import com.carpentersblocks.block.slope.SlopeKind;
import com.carpentersblocks.network.SlopeSelectPayload;
import com.carpentersblocks.config.CBConfig;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.registry.SlopeContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 2: Carpenter's Slope placement, joining, hammer use, covers, shapes and drops. */
public class SlopeTests {

    private static final BlockPos FLOOR = new BlockPos(1, 0, 1);
    private static final BlockPos ON_FLOOR = FLOOR.above();

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static Slope slopeAt(GameTestHelper helper, BlockPos pos) {
        BlockState state = helper.getBlockState(pos);
        if (!(state.getBlock() instanceof CarpentersSlopeBlock)) {
            helper.fail("no slope at " + pos + ": " + state);
        }
        return state.getValue(CarpentersSlopeBlock.SLOPE);
    }

    private static void setSlope(GameTestHelper helper, BlockPos pos, Slope slope) {
        helper.setBlock(pos, SlopeContent.CARPENTERS_SLOPE.defaultBlockState()
                .setValue(CarpentersSlopeBlock.SLOPE, slope));
    }

    private static ItemStack slopeItem(SlopeKind kind) {
        return SlopeItem.stack(SlopeContent.CARPENTERS_SLOPE, kind);
    }

    /** A player looking in {@code look}, holding {@code stack}. */
    private static Player player(GameTestHelper helper, GameType mode, Direction look, ItemStack stack) {
        Player player = helper.makeMockPlayer(mode);
        player.setYRot(look.toYRot());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return player;
    }

    /** Right-clicks the middle of a face of the block at {@code pos} with the held item, as placing a block does. */
    private static void useItemOn(GameTestHelper helper, Player player, BlockPos pos, Direction face) {
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, pos, face)));
    }

    private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction face) {
        Vec3 center = Vec3.atCenterOf(helper.absolutePos(pos));
        Vec3 onFace = center.add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5));
        return new BlockHitResult(onFace, face, helper.absolutePos(pos), false);
    }

    @CBGameTest
    public void wedgePlacedOnTopRisesAwayFromThePlayer(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        useItemOn(helper, player(helper, GameType.CREATIVE, Direction.SOUTH, slopeItem(SlopeKind.WEDGE)), FLOOR,
                Direction.UP);
        check(helper, slopeAt(helper, ON_FLOOR) == Slope.WEDGE_POS_N, "expected an upward wedge facing north");
        helper.succeed();
    }

    @CBGameTest
    public void wedgePlacedUnderACeilingIsUpsideDown(GameTestHelper helper) {
        BlockPos ceiling = new BlockPos(1, 2, 1);
        helper.setBlock(ceiling, Blocks.STONE);
        useItemOn(helper, player(helper, GameType.CREATIVE, Direction.WEST, slopeItem(SlopeKind.WEDGE)), ceiling,
                Direction.DOWN);
        check(helper, slopeAt(helper, ceiling.below()) == Slope.WEDGE_NEG_E, "expected a downward wedge");
        helper.succeed();
    }

    @CBGameTest
    public void eachItemKindPlacesItsType(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        for (SlopeKind kind : SlopeKind.values()) {
            helper.setBlock(ON_FLOOR, Blocks.AIR);
            Player player = player(helper, GameType.CREATIVE, Direction.SOUTH, slopeItem(kind));
            player.setShiftKeyDown(true);
            useItemOn(helper, player, FLOOR, Direction.UP);
            Slope placed = slopeAt(helper, ON_FLOOR);
            check(helper, SlopeKind.of(placed) == kind, kind + " placed " + placed);
            check(helper, placed.isPositive, kind + " placed on a floor must point up");
        }
        helper.succeed();
    }

    @CBGameTest
    public void placingAWedgeBesideAWedgeMakesACorner(GameTestHelper helper) {
        BlockPos north = new BlockPos(1, 1, 0);
        setSlope(helper, north, Slope.WEDGE_POS_N);
        helper.setBlock(FLOOR, Blocks.STONE);
        useItemOn(helper, player(helper, GameType.CREATIVE, Direction.EAST, slopeItem(SlopeKind.WEDGE)), FLOOR,
                Direction.UP);
        Slope placed = slopeAt(helper, ON_FLOOR);
        Slope neighbor = slopeAt(helper, north);
        check(helper, placed.type == Slope.Type.WEDGE || placed.type == Slope.Type.WEDGE_INT
                || placed.type == Slope.Type.WEDGE_EXT, "placed slope must stay a wedge: " + placed);
        check(helper, neighbor.type == Slope.Type.WEDGE_INT || neighbor.type == Slope.Type.WEDGE_EXT
                || placed.type != Slope.Type.WEDGE, "one of the two must turn into a corner: " + placed + ", "
                + neighbor);
        helper.succeed();
    }

    @CBGameTest
    public void sneakingSkipsJoining(GameTestHelper helper) {
        BlockPos north = new BlockPos(1, 1, 0);
        setSlope(helper, north, Slope.WEDGE_POS_N);
        helper.setBlock(FLOOR, Blocks.STONE);
        Player player = player(helper, GameType.CREATIVE, Direction.EAST, slopeItem(SlopeKind.WEDGE));
        player.setShiftKeyDown(true);
        useItemOn(helper, player, FLOOR, Direction.UP);
        check(helper, slopeAt(helper, ON_FLOOR) == Slope.WEDGE_POS_W, "placed wedge keeps its orientation");
        check(helper, slopeAt(helper, north) == Slope.WEDGE_POS_N, "neighbor must stay as it was");
        helper.succeed();
    }

    @CBGameTest
    public void prismsJoinIntoARidge(GameTestHelper helper) {
        BlockPos north = new BlockPos(1, 1, 0);
        setSlope(helper, north, Slope.PRISM_POS);
        helper.setBlock(FLOOR, Blocks.STONE);
        useItemOn(helper, player(helper, GameType.CREATIVE, Direction.SOUTH, slopeItem(SlopeKind.PRISM)), FLOOR,
                Direction.UP);
        check(helper, slopeAt(helper, ON_FLOOR) != Slope.PRISM_POS, "placed prism must join its neighbor");
        check(helper, slopeAt(helper, north) != Slope.PRISM_POS, "neighbor prism must join the placed one");
        helper.succeed();
    }

    @CBGameTest
    public void placedInWaterIsWaterlogged(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        helper.setBlock(ON_FLOOR, Blocks.WATER);
        useItemOn(helper, player(helper, GameType.CREATIVE, Direction.NORTH, slopeItem(SlopeKind.WEDGE)), FLOOR,
                Direction.UP);
        slopeAt(helper, ON_FLOOR);
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersSlopeBlock.WATERLOGGED), "not waterlogged");
        helper.succeed();
    }

    @CBGameTest
    public void hammerTurnsAndChangesSlopes(GameTestHelper helper) {
        setSlope(helper, ON_FLOOR, Slope.WEDGE_POS_N);
        Player player = player(helper, GameType.CREATIVE, Direction.SOUTH, new ItemStack(ModContent.CARPENTERS_HAMMER));
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(ON_FLOOR);

        SlopeContent.CARPENTERS_SLOPE.attack(level.getBlockState(pos), level, pos, player, Direction.UP);
        check(helper, slopeAt(helper, ON_FLOOR) == Slope.WEDGE_POS_E, "left-click turns the slope clockwise");

        SlopeContent.CARPENTERS_SLOPE.use(level.getBlockState(pos), level, pos, player, InteractionHand.MAIN_HAND,
                hit(helper, ON_FLOOR, Direction.UP));
        check(helper, slopeAt(helper, ON_FLOOR) == Slope.WEDGE_NEG_E, "right-click turns it upside down");
        helper.succeed();
    }

    @CBGameTest
    public void sideCoversGoOnlyOnFullFaces(GameTestHelper helper) {
        setSlope(helper, ON_FLOOR, Slope.WEDGE_POS_N);
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(ON_FLOOR);
        Player player = player(helper, GameType.CREATIVE, Direction.SOUTH, new ItemStack(Blocks.STONE, 3));
        for (Direction face : List.of(Direction.UP, Direction.UP, Direction.SOUTH)) {
            SlopeContent.CARPENTERS_SLOPE.use(level.getBlockState(pos), level, pos, player,
                    InteractionHand.MAIN_HAND, hit(helper, ON_FLOOR, face));
        }
        CoverData data = ((CoverableBlockEntity) helper.getBlockEntity(ON_FLOOR)).getCoverData();
        check(helper, data.hasCover(CoverData.BASE), "first cover goes on the base");
        check(helper, !data.hasCover(Direction.UP.get3DDataValue()), "the sloped top takes no side cover");
        check(helper, data.hasCover(Direction.SOUTH.get3DDataValue()), "the full back takes a side cover");
        helper.succeed();
    }

    @CBGameTest
    public void opaqueCoversMakeSlopesSolid(GameTestHelper helper) {
        setSlope(helper, ON_FLOOR, Slope.WEDGE_POS_N);
        CoverableBlockEntity be = helper.getBlockEntity(ON_FLOOR);
        check(helper, !helper.getBlockState(ON_FLOOR).getValue(CarpentersSlopeBlock.SOLID), "bare frame is not solid");
        be.setCover(CoverData.BASE, Blocks.STONE.defaultBlockState());
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersSlopeBlock.SOLID), "stone slope is solid");
        helper.succeed();
    }

    @CBGameTest
    public void collisionFollowsTheSlope(GameTestHelper helper) {
        setSlope(helper, ON_FLOOR, Slope.WEDGE_POS_N);
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(ON_FLOOR);
        BlockState state = level.getBlockState(pos);
        check(helper, state.isFaceSturdy(level, pos, Direction.DOWN), "bottom is a full face");
        check(helper, state.isFaceSturdy(level, pos, Direction.SOUTH), "back is a full face");
        check(helper, !state.isFaceSturdy(level, pos, Direction.UP), "top is sloped");
        check(helper, !state.isFaceSturdy(level, pos, Direction.NORTH), "front is open");
        check(helper, state.getCollisionShape(level, pos).bounds().maxY == 1.0, "slope is a full block tall");
        helper.succeed();
    }

    @CBGameTest
    public void dropsKeepTheItemKind(GameTestHelper helper) {
        Level level = helper.getLevel();
        for (Slope slope : List.of(Slope.WEDGE_INT_POS_SE, Slope.OBL_EXT_NEG_NW, Slope.PRISM_3P_POS_NSE,
                Slope.PRISM_WEDGE_POS_W, Slope.WEDGE_SE)) {
            setSlope(helper, ON_FLOOR, slope);
            BlockPos pos = helper.absolutePos(ON_FLOOR);
            List<ItemStack> drops = Block.getDrops(level.getBlockState(pos), helper.getLevel(), pos,
                    level.getBlockEntity(pos));
            SlopeKind expected = SlopeKind.of(slope);
            check(helper, drops.size() == 1 && drops.get(0).is(SlopeContent.CARPENTERS_SLOPE_ITEM)
                    && SlopeItem.kind(drops.get(0)) == expected, slope + " must drop a " + expected + ": " + drops);
            ItemStack picked = SlopeContent.CARPENTERS_SLOPE.getCloneItemStack(level, pos, level.getBlockState(pos));
            check(helper, SlopeItem.kind(picked) == expected, slope + " must pick a " + expected);
        }
        helper.succeed();
    }

    @CBGameTest
    public void slopeSelectCyclesTheHeldKind(GameTestHelper helper) {
        Player player = player(helper, GameType.SURVIVAL, Direction.SOUTH, slopeItem(SlopeKind.WEDGE));
        player.getInventory().setItem(1, new ItemStack(Blocks.STONE));
        int slot = player.getInventory().selected;

        SlopeSelectPayload.handle(new SlopeSelectPayload(slot, true), player);
        check(helper, SlopeItem.kind(player.getMainHandItem()) == SlopeKind.OBLIQUE_INTERIOR, "scroll up: next kind");
        SlopeSelectPayload.handle(new SlopeSelectPayload(slot, false), player);
        SlopeSelectPayload.handle(new SlopeSelectPayload(slot, false), player);
        check(helper, SlopeItem.kind(player.getMainHandItem()) == SlopeKind.PRISM_WEDGE, "scroll down wraps around");

        SlopeSelectPayload.handle(new SlopeSelectPayload(1, true), player);
        check(helper, player.getInventory().getItem(1).getComponentsPatch().isEmpty(), "other items are left alone");
        SlopeSelectPayload.handle(new SlopeSelectPayload(40, true), player);
        SlopeSelectPayload.handle(new SlopeSelectPayload(-1, true), player);
        check(helper, SlopeItem.kind(player.getMainHandItem()) == SlopeKind.PRISM_WEDGE, "bad slots are ignored");
        helper.succeed();
    }

    @CBGameTest
    public void ascendingRailsGetASlopeFill(GameTestHelper helper) {
        boolean enabled = CBConfig.enableRailSlopes;
        try {
            CBConfig.enableRailSlopes = true;
            BlockPos rail = ON_FLOOR.above();
            helper.setBlock(ON_FLOOR, ModContent.CARPENTERS_BLOCK.defaultBlockState());
            helper.setBlock(rail.east(), Blocks.STONE);
            helper.setBlock(rail, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.ASCENDING_EAST));
            Level level = helper.getLevel();
            BlockPos pos = helper.absolutePos(ON_FLOOR);
            check(helper, helper.getBlockState(rail).getValue(RailBlock.SHAPE) == RailShape.ASCENDING_EAST,
                    "rail must stay ascending");
            check(helper, RailSlopes.slopeAbove(level, pos, level.getBlockState(pos)) == Slope.WEDGE_POS_W,
                    "a rail rising east fills a wedge high on the east");

            CBConfig.enableRailSlopes = false;
            check(helper, RailSlopes.slopeAbove(level, pos, level.getBlockState(pos)) == null, "config turns it off");
            CBConfig.enableRailSlopes = true;

            helper.setBlock(rail, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST));
            check(helper, RailSlopes.slopeAbove(level, pos, level.getBlockState(pos)) == null, "flat rails get none");
        } finally {
            CBConfig.enableRailSlopes = enabled;
        }
        helper.succeed();
    }
}
