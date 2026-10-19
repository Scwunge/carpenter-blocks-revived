package com.carpentersblocks.test.door;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.door.CarpentersDoorBlock;
import com.carpentersblocks.block.door.CarpentersGarageDoorBlock;
import com.carpentersblocks.block.door.CarpentersHatchBlock;
import com.carpentersblocks.block.door.DoorType;
import com.carpentersblocks.block.door.GarageDoorType;
import com.carpentersblocks.block.door.HatchType;
import com.carpentersblocks.registry.DoorContent;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 7: Carpenter's Door, Hatch and Garage Door placement, opening, redstone, the hammer and drops. */
public class DoorTests {

    private static final BlockPos LOWER = new BlockPos(1, 1, 1);
    private static final BlockPos UPPER = new BlockPos(1, 2, 1);

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    /** A hit on {@code face} of the block at {@code pos}, {@code height} (0 to 1) up the block. */
    private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction face, double height) {
        Vec3 center = Vec3.atCenterOf(helper.absolutePos(pos));
        Vec3 onFace = center.add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5));
        if (face.getAxis() != Direction.Axis.Y) {
            onFace = new Vec3(onFace.x, helper.absolutePos(pos).getY() + height, onFace.z);
        }
        return new BlockHitResult(onFace, face, helper.absolutePos(pos), false);
    }

    private static Player player(GameTestHelper helper, GameType mode, Direction look, ItemStack held) {
        Player player = helper.makeMockPlayer(mode);
        player.setYRot(look.toYRot());
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        return player;
    }

    private static void place(GameTestHelper helper, Player player, BlockPos against, Direction face, double height) {
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                hit(helper, against, face, height)));
    }

    private static void use(GameTestHelper helper, Player player, BlockPos pos) {
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(pos);
        BlockState state = level.getBlockState(abs);
        ((CoverableBlock) state.getBlock()).use(state, level, abs, player, InteractionHand.MAIN_HAND,
                hit(helper, pos, Direction.NORTH, 0.5));
    }

    private static void attack(GameTestHelper helper, Player player, BlockPos pos) {
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(pos);
        BlockState state = level.getBlockState(abs);
        ((CoverableBlock) state.getBlock()).attack(state, level, abs, player, Direction.NORTH);
    }

    private static ItemStack hammer() {
        return new ItemStack(ModContent.CARPENTERS_HAMMER);
    }

    // Door

    /** Stone under {@link #LOWER} and a door placed on it looking south. */
    private static Player placeDoor(GameTestHelper helper, GameType mode) {
        helper.setBlock(LOWER.below(), Blocks.STONE);
        Player player = player(helper, mode, Direction.SOUTH, new ItemStack(DoorContent.CARPENTERS_DOOR));
        place(helper, player, LOWER.below(), Direction.UP, 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return player;
    }

    @CBGameTest
    public void doorPlacesBothHalves(GameTestHelper helper) {
        placeDoor(helper, GameType.CREATIVE);
        BlockState lower = helper.getBlockState(LOWER);
        BlockState upper = helper.getBlockState(UPPER);
        check(helper, lower.is(DoorContent.CARPENTERS_DOOR) && CarpentersDoorBlock.isLower(lower), "lower half");
        check(helper, upper.is(DoorContent.CARPENTERS_DOOR) && !CarpentersDoorBlock.isLower(upper), "upper half");
        check(helper, lower.getValue(CarpentersDoorBlock.FACING) == Direction.SOUTH, "faces the way the player looked");
        check(helper, CarpentersDoorBlock.side(lower) == Direction.NORTH, "closed against the far face");
        check(helper, CarpentersDoorBlock.hingeLeft(lower) && CarpentersDoorBlock.hingeLeft(upper), "hinged left");
        helper.succeed();
    }

    @CBGameTest
    public void doorNeedsSolidGround(GameTestHelper helper) {
        Player player = player(helper, GameType.CREATIVE, Direction.SOUTH, new ItemStack(DoorContent.CARPENTERS_DOOR));
        helper.setBlock(LOWER.below(), Blocks.OAK_FENCE);
        place(helper, player, LOWER.below(), Direction.UP, 1);
        check(helper, helper.getBlockState(LOWER).isAir(), "not placed on a fence");
        helper.succeed();
    }

    @CBGameTest
    public void doorOpensByHandUnlessRigid(GameTestHelper helper) {
        Player player = placeDoor(helper, GameType.SURVIVAL);
        use(helper, player, LOWER);
        check(helper, helper.getBlockState(LOWER).getValue(CarpentersDoorBlock.OPEN), "lower opens");
        check(helper, helper.getBlockState(UPPER).getValue(CarpentersDoorBlock.OPEN), "upper follows");
        check(helper, CarpentersDoorBlock.side(helper.getBlockState(LOWER)) == Direction.EAST,
                "a left hinge swings to the placer's left");
        use(helper, player, UPPER);
        check(helper, !helper.getBlockState(LOWER).getValue(CarpentersDoorBlock.OPEN), "closes again");

        player.setItemInHand(InteractionHand.MAIN_HAND, hammer());
        player.setShiftKeyDown(true);
        use(helper, player, LOWER);
        check(helper, helper.getBlockState(UPPER).getValue(CarpentersDoorBlock.RIGID), "sneak hammer: rigid");
        player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, player, LOWER);
        check(helper, !helper.getBlockState(LOWER).getValue(CarpentersDoorBlock.OPEN), "rigid: no opening by hand");
        helper.succeed();
    }

    @CBGameTest
    public void doorFollowsRedstone(GameTestHelper helper) {
        placeDoor(helper, GameType.CREATIVE);
        helper.setBlock(new BlockPos(0, 2, 1), Blocks.REDSTONE_BLOCK);
        check(helper, helper.getBlockState(LOWER).getValue(CarpentersDoorBlock.OPEN), "power on the upper opens it");
        helper.setBlock(new BlockPos(0, 2, 1), Blocks.AIR);
        check(helper, !helper.getBlockState(LOWER).getValue(CarpentersDoorBlock.OPEN), "and it closes after");
        helper.succeed();
    }

    @CBGameTest
    public void doorHammerSwapsHingeAndStyle(GameTestHelper helper) {
        Player player = placeDoor(helper, GameType.CREATIVE);
        player.setItemInHand(InteractionHand.MAIN_HAND, hammer());
        attack(helper, player, LOWER);
        check(helper, helper.getBlockState(UPPER).getValue(CarpentersDoorBlock.HINGE) == DoorHingeSide.RIGHT,
                "left-click: hinge on the right, both halves");
        use(helper, player, UPPER);
        check(helper, helper.getBlockState(LOWER).getValue(CarpentersDoorBlock.TYPE) == DoorType.GLASS_TALL,
                "right-click: next style, both halves");
        helper.succeed();
    }

    @CBGameTest
    public void doubleDoorsOpenTogether(GameTestHelper helper) {
        BlockPos left = new BlockPos(2, 1, 1);
        helper.setBlock(left.below(), Blocks.STONE);
        helper.setBlock(LOWER.below(), Blocks.STONE);
        Player player = player(helper, GameType.CREATIVE, Direction.SOUTH, new ItemStack(DoorContent.CARPENTERS_DOOR, 2));
        place(helper, player, left.below(), Direction.UP, 1);
        place(helper, player, LOWER.below(), Direction.UP, 1);
        check(helper, helper.getBlockState(LOWER).getValue(CarpentersDoorBlock.HINGE) == DoorHingeSide.RIGHT,
                "the second door hinges right to pair up");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, player, LOWER);
        check(helper, helper.getBlockState(left).getValue(CarpentersDoorBlock.OPEN)
                && helper.getBlockState(left.above()).getValue(CarpentersDoorBlock.OPEN), "both doors open");
        helper.succeed();
    }

    @CBGameTest
    public void doorDropsOnce(GameTestHelper helper) {
        placeDoor(helper, GameType.SURVIVAL);
        helper.getLevel().destroyBlock(helper.absolutePos(UPPER), true);
        check(helper, helper.getBlockState(LOWER).isAir(), "both halves go");
        helper.assertItemEntityCountIs(DoorContent.CARPENTERS_DOOR.asItem(), LOWER, 3, 1);
        helper.succeed();
    }

    // Hatch

    private static final BlockPos HATCH = new BlockPos(1, 1, 1);
    private static final BlockPos SOUTH_WALL = new BlockPos(1, 1, 2);
    private static final BlockPos NORTH_WALL = new BlockPos(1, 1, 0);

    private static Player placeHatch(GameTestHelper helper, double height) {
        helper.setBlock(SOUTH_WALL, Blocks.STONE);
        Player player = player(helper, GameType.CREATIVE, Direction.SOUTH, new ItemStack(DoorContent.CARPENTERS_HATCH));
        place(helper, player, SOUTH_WALL, Direction.NORTH, height);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return player;
    }

    @CBGameTest
    public void hatchHangsHighOrLow(GameTestHelper helper) {
        placeHatch(helper, 0.75);
        BlockState state = helper.getBlockState(HATCH);
        check(helper, state.is(DoorContent.CARPENTERS_HATCH), "placed on the wall");
        check(helper, state.getValue(CarpentersHatchBlock.FACING) == Direction.SOUTH, "hangs on the wall's side");
        check(helper, CarpentersHatchBlock.isHigh(state), "upper half clicked: high");
        helper.setBlock(HATCH, Blocks.AIR);
        placeHatch(helper, 0.25);
        check(helper, !CarpentersHatchBlock.isHigh(helper.getBlockState(HATCH)), "lower half clicked: low");
        helper.succeed();
    }

    @CBGameTest
    public void hatchOpensAgainstItsSupport(GameTestHelper helper) {
        Player player = placeHatch(helper, 0.25);
        use(helper, player, HATCH);
        BlockState state = helper.getBlockState(HATCH);
        check(helper, state.getValue(CarpentersHatchBlock.OPEN), "opens by hand");
        var bounds = state.getShape(helper.getLevel(), helper.absolutePos(HATCH), CollisionContext.empty()).bounds();
        check(helper, bounds.minZ > 0.8 && bounds.maxY == 1, "stands up against the south wall");
        helper.succeed();
    }

    @CBGameTest
    public void hatchHammerMovesItAndCyclesStyle(GameTestHelper helper) {
        Player player = placeHatch(helper, 0.25);
        helper.setBlock(NORTH_WALL, Blocks.STONE);
        player.setItemInHand(InteractionHand.MAIN_HAND, hammer());
        attack(helper, player, HATCH);
        check(helper, helper.getBlockState(HATCH).getValue(CarpentersHatchBlock.FACING) == Direction.NORTH,
                "left-click: onto the next wall");
        use(helper, player, HATCH);
        check(helper, helper.getBlockState(HATCH).getValue(CarpentersHatchBlock.TYPE) == HatchType.WINDOW,
                "right-click: next style");
        helper.succeed();
    }

    @CBGameTest
    public void hatchMovesOrDropsWhenItsWallGoes(GameTestHelper helper) {
        placeHatch(helper, 0.25);
        helper.setBlock(NORTH_WALL, Blocks.STONE);
        helper.setBlock(SOUTH_WALL, Blocks.AIR);
        check(helper, helper.getBlockState(HATCH).getValue(CarpentersHatchBlock.FACING) == Direction.NORTH,
                "moves to the other wall");
        helper.setBlock(NORTH_WALL, Blocks.AIR);
        check(helper, helper.getBlockState(HATCH).isAir(), "drops with nothing to hang on");
        helper.succeed();
    }

    @CBGameTest
    public void openHiddenHighHatchIsALadder(GameTestHelper helper) {
        Player player = placeHatch(helper, 0.75);
        BlockPos abs = helper.absolutePos(HATCH);
        CarpentersHatchBlock hatch = DoorContent.CARPENTERS_HATCH;
        check(helper, !hatch.isLadder(helper.getBlockState(HATCH), helper.getLevel(), abs, player), "closed: no");
        use(helper, player, HATCH);
        check(helper, hatch.isLadder(helper.getBlockState(HATCH), helper.getLevel(), abs, player), "open: climbable");
        helper.succeed();
    }

    // Garage door

    private static final BlockPos CEILING = new BlockPos(1, 2, 1);
    private static final BlockPos HOST = new BlockPos(1, 1, 1);
    private static final BlockPos BOTTOM = new BlockPos(1, 0, 1);

    /** A garage door hung from a ceiling, filling down two blocks to a floor. */
    private static Player placeGarageDoor(GameTestHelper helper, GameType mode, BlockPos ceiling) {
        helper.setBlock(ceiling, Blocks.STONE);
        helper.setBlock(ceiling.below(3), Blocks.STONE);
        Player player = player(helper, mode, Direction.SOUTH, new ItemStack(DoorContent.CARPENTERS_GARAGE_DOOR));
        place(helper, player, ceiling, Direction.DOWN, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return player;
    }

    @CBGameTest
    public void garageDoorFillsDownFromItsHost(GameTestHelper helper) {
        placeGarageDoor(helper, GameType.CREATIVE, CEILING);
        BlockState host = helper.getBlockState(HOST);
        BlockState bottom = helper.getBlockState(BOTTOM);
        check(helper, host.is(DoorContent.CARPENTERS_GARAGE_DOOR) && host.getValue(CarpentersGarageDoorBlock.HOST),
                "the clicked piece hosts");
        check(helper, bottom.is(DoorContent.CARPENTERS_GARAGE_DOOR) && !bottom.getValue(CarpentersGarageDoorBlock.HOST),
                "a piece fills the space below");
        check(helper, bottom.getValue(CarpentersGarageDoorBlock.FACING) == Direction.SOUTH, "facing the same way");
        helper.succeed();
    }

    @CBGameTest
    public void garageDoorRollsUp(GameTestHelper helper) {
        Player player = placeGarageDoor(helper, GameType.CREATIVE, CEILING);
        use(helper, player, BOTTOM);
        check(helper, helper.getBlockState(HOST).getValue(CarpentersGarageDoorBlock.OPEN)
                && helper.getBlockState(BOTTOM).getValue(CarpentersGarageDoorBlock.OPEN), "every piece opens");
        BlockPos bottom = helper.absolutePos(BOTTOM);
        check(helper, helper.getBlockState(BOTTOM).getShape(helper.getLevel(), bottom, CollisionContext.empty())
                .isEmpty(), "rolled-up pieces can't be touched");
        var bounds = helper.getBlockState(HOST).getShape(helper.getLevel(), helper.absolutePos(HOST),
                CollisionContext.empty()).bounds();
        check(helper, bounds.minY == 0.5, "the host holds the door in its top half");
        use(helper, player, HOST);
        check(helper, !helper.getBlockState(BOTTOM).getValue(CarpentersGarageDoorBlock.OPEN), "closes again");
        helper.succeed();
    }

    @CBGameTest
    public void garageDoorFollowsRedstone(GameTestHelper helper) {
        placeGarageDoor(helper, GameType.CREATIVE, CEILING);
        // East of the bottom piece: power on the west, at (0, 0, 1), would reach the test's structure block
        helper.setBlock(new BlockPos(2, 0, 1), Blocks.REDSTONE_BLOCK);
        check(helper, helper.getBlockState(HOST).getValue(CarpentersGarageDoorBlock.OPEN), "power on a piece opens");
        helper.setBlock(new BlockPos(2, 0, 1), Blocks.AIR);
        check(helper, !helper.getBlockState(HOST).getValue(CarpentersGarageDoorBlock.OPEN), "and it closes after");
        helper.succeed();
    }

    @CBGameTest
    public void garageDoorHammerChangesEveryPiece(GameTestHelper helper) {
        Player player = placeGarageDoor(helper, GameType.CREATIVE, CEILING);
        player.setItemInHand(InteractionHand.MAIN_HAND, hammer());
        attack(helper, player, BOTTOM);
        check(helper, helper.getBlockState(HOST).getValue(CarpentersGarageDoorBlock.TYPE) == GarageDoorType.GLASS_TOP,
                "left-click: next style");
        use(helper, player, HOST);
        use(helper, player, HOST);
        check(helper, helper.getBlockState(BOTTOM).getValue(CarpentersGarageDoorBlock.TYPE) == GarageDoorType.HIDDEN,
                "right-click: back, wrapping round");
        player.setShiftKeyDown(true);
        use(helper, player, HOST);
        check(helper, helper.getBlockState(BOTTOM).getValue(CarpentersGarageDoorBlock.RIGID), "sneak: rigid");
        helper.succeed();
    }

    @CBGameTest
    public void garageDoorsSideBySideActAsOne(GameTestHelper helper) {
        Player player = placeGarageDoor(helper, GameType.CREATIVE, CEILING);
        player.setItemInHand(InteractionHand.MAIN_HAND, hammer());
        attack(helper, player, HOST);
        placeGarageDoor(helper, GameType.CREATIVE, new BlockPos(0, 2, 1));
        check(helper, helper.getBlockState(new BlockPos(0, 1, 1)).getValue(CarpentersGarageDoorBlock.TYPE)
                == GarageDoorType.GLASS_TOP, "takes the style of the door beside it");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        use(helper, player, HOST);
        check(helper, helper.getBlockState(new BlockPos(0, 0, 1)).getValue(CarpentersGarageDoorBlock.OPEN),
                "opening one opens the other");
        helper.succeed();
    }

    @CBGameTest
    public void garageDoorBreaksWholeAndDropsOnce(GameTestHelper helper) {
        placeGarageDoor(helper, GameType.SURVIVAL, CEILING);
        Player player = player(helper, GameType.SURVIVAL, Direction.SOUTH, ItemStack.EMPTY);
        BlockPos bottom = helper.absolutePos(BOTTOM);
        BlockState state = helper.getBlockState(BOTTOM);
        state.getBlock().playerWillDestroy(helper.getLevel(), bottom, state, player);
        helper.getLevel().destroyBlock(bottom, true);
        check(helper, helper.getBlockState(HOST).isAir(), "the column goes");
        helper.assertItemEntityCountIs(DoorContent.CARPENTERS_GARAGE_DOOR.asItem(), HOST, 3, 1);
        helper.succeed();
    }

    @CBGameTest
    public void garageDoorFillsAGapBelow(GameTestHelper helper) {
        helper.setBlock(CEILING, Blocks.STONE);
        helper.setBlock(BOTTOM, Blocks.STONE);
        helper.setBlock(BOTTOM.below(), Blocks.STONE);
        Player player = player(helper, GameType.CREATIVE, Direction.SOUTH,
                new ItemStack(DoorContent.CARPENTERS_GARAGE_DOOR));
        place(helper, player, CEILING, Direction.DOWN, 0);
        helper.setBlock(BOTTOM, Blocks.AIR);
        check(helper, helper.getBlockState(BOTTOM).is(DoorContent.CARPENTERS_GARAGE_DOOR), "the gap fills");
        helper.succeed();
    }
}
