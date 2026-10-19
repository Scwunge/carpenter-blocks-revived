package com.carpentersblocks.test.collapsible;

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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.carpentersblocks.block.collapsible.CarpentersCollapsibleBlock;
import com.carpentersblocks.block.collapsible.CollapsibleGeometry.Depths;
import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.registry.CollapsibleContent;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 9: Carpenter's Collapsible Block placement, sloping, the hammer, smoothing, shapes and drops. */
public class CollapsibleTests {

    private static final BlockPos CELL = new BlockPos(1, 1, 1);

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 3; ++x) {
            for (int z = 0; z < 3; ++z) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    private static BlockState state(Direction facing) {
        return CollapsibleContent.CARPENTERS_COLLAPSIBLE_BLOCK.defaultBlockState()
                .setValue(CarpentersCollapsibleBlock.FACING, facing);
    }

    private static CoverableBlockEntity be(GameTestHelper helper, BlockPos pos) {
        return (CoverableBlockEntity) helper.getBlockEntity(pos);
    }

    private static Depths depths(GameTestHelper helper, BlockPos pos) {
        return Depths.of(be(helper, pos).getBlockData());
    }

    private static void place(GameTestHelper helper, BlockPos pos, Direction facing, Depths depths) {
        helper.setBlock(pos, state(facing));
        CarpentersCollapsibleBlock.setDepths(be(helper, pos), depths);
    }

    /** Uses the block's item on {@code face} of {@code against} at {@code hitY} up that block. */
    private static void useItem(GameTestHelper helper, Player player, BlockPos against, Direction face, double hitY) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CollapsibleContent.CARPENTERS_COLLAPSIBLE_BLOCK));
        BlockPos abs = helper.absolutePos(against);
        Vec3 at = Vec3.atCenterOf(abs).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5))
                .add(0, face.getAxis() == Direction.Axis.Y ? 0 : hitY - 0.5, 0);
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(at, face, abs, false)));
    }

    /** Holds the hammer above {@code pos}, looking straight down at {@code x, z} across it. */
    private static Player aim(GameTestHelper helper, BlockPos pos, double x, double z) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos abs = helper.absolutePos(pos);
        player.moveTo(abs.getX() + x, abs.getY() + 1.0, abs.getZ() + z, 0, 90);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        return player;
    }

    private static void rightClick(GameTestHelper helper, Player player, BlockPos pos) {
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(pos);
        CollapsibleContent.CARPENTERS_COLLAPSIBLE_BLOCK.use(level.getBlockState(abs), level, abs, player,
                InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
    }

    private static void leftClick(GameTestHelper helper, Player player, BlockPos pos) {
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(pos);
        CollapsibleContent.CARPENTERS_COLLAPSIBLE_BLOCK.attack(level.getBlockState(abs), level, abs, player,
                Direction.UP);
    }

    private static boolean sturdy(GameTestHelper helper, BlockPos pos, Direction side) {
        BlockPos abs = helper.absolutePos(pos);
        return helper.getLevel().getBlockState(abs).isFaceSturdy(helper.getLevel(), abs, side);
    }

    private static boolean solidAt(VoxelShape shape, double x, double y, double z) {
        return shape.toAabbs().stream().anyMatch(box -> box.contains(x, y, z));
    }

    @CBGameTest
    public void placedFacingTheClickedFace(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        useItem(helper, player, new BlockPos(1, 0, 1), Direction.UP, 1);
        helper.assertBlockProperty(CELL, CarpentersCollapsibleBlock.FACING, Direction.UP);
        helper.setBlock(CELL, Blocks.AIR);
        useItem(helper, player, new BlockPos(1, 2, 1), Direction.DOWN, 0);
        helper.assertBlockProperty(CELL, CarpentersCollapsibleBlock.FACING, Direction.DOWN);
        // On a side, the upper half hangs and the lower half stands
        BlockPos wall = CELL.north();
        helper.setBlock(wall, Blocks.STONE);
        helper.setBlock(CELL, Blocks.AIR);
        useItem(helper, player, wall, Direction.SOUTH, 0.8);
        helper.assertBlockProperty(CELL, CarpentersCollapsibleBlock.FACING, Direction.DOWN);
        helper.setBlock(CELL, Blocks.AIR);
        useItem(helper, player, wall, Direction.SOUTH, 0.2);
        helper.assertBlockProperty(CELL, CarpentersCollapsibleBlock.FACING, Direction.UP);
        helper.succeed();
    }

    @CBGameTest
    public void sneakingPlacesAFullBlock(GameTestHelper helper) {
        floor(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        useItem(helper, player, new BlockPos(1, 0, 1), Direction.UP, 1);
        check(helper, depths(helper, CELL).equals(Depths.FULL), "full: " + depths(helper, CELL));
        check(helper, sturdy(helper, CELL, Direction.UP) && sturdy(helper, CELL, Direction.NORTH),
                "a full block's faces are whole");
        helper.succeed();
    }

    @CBGameTest
    public void placementSlopesBetweenTheGround(GameTestHelper helper) {
        floor(helper);
        // A wall to the west; drops to the east, north and south end the slope at the floor
        helper.setBlock(new BlockPos(0, 1, 1), Blocks.STONE);
        helper.setBlock(new BlockPos(2, 0, 1), Blocks.AIR);
        helper.setBlock(new BlockPos(1, 0, 0), Blocks.AIR);
        helper.setBlock(new BlockPos(1, 0, 2), Blocks.AIR);
        useItem(helper, helper.makeMockPlayer(GameType.SURVIVAL), new BlockPos(1, 0, 1), Direction.UP, 1);
        check(helper, depths(helper, CELL).equals(new Depths(16, 16, 0, 0)),
                "falls from the wall to the drop: " + depths(helper, CELL));

        // Next to a collapsible block, it carries on from that block's corners
        helper.setBlock(CELL, Blocks.AIR);
        place(helper, new BlockPos(0, 1, 1), Direction.UP, new Depths(16, 16, 8, 8));
        useItem(helper, helper.makeMockPlayer(GameType.SURVIVAL), new BlockPos(1, 0, 1), Direction.UP, 1);
        check(helper, depths(helper, CELL).equals(new Depths(8, 8, 0, 0)), "from 8 down: " + depths(helper, CELL));
        helper.succeed();
    }

    @CBGameTest
    public void hammerMovesTheCornerUnderTheCursor(GameTestHelper helper) {
        floor(helper);
        place(helper, CELL, Direction.UP, new Depths(8, 8, 8, 8));
        Player player = aim(helper, CELL, 0.25, 0.25);
        rightClick(helper, player, CELL);
        check(helper, depths(helper, CELL).equals(new Depths(9, 8, 8, 8)), "right-click raises the north-west corner");
        leftClick(helper, player, CELL);
        leftClick(helper, player, CELL);
        check(helper, depths(helper, CELL).equals(new Depths(7, 8, 8, 8)), "left-click lowers it");

        player = aim(helper, CELL, 0.75, 0.75);
        place(helper, CELL, Direction.UP, new Depths(8, 8, 8, 16));
        rightClick(helper, player, CELL);
        check(helper, depths(helper, CELL).pp() == 16, "no higher than a block");

        // Hanging, the hammer works the other way round
        place(helper, CELL, Direction.DOWN, new Depths(8, 8, 8, 8));
        rightClick(helper, player, CELL);
        check(helper, depths(helper, CELL).equals(new Depths(8, 8, 8, 7)), "hanging: right-click shortens");
        leftClick(helper, player, CELL);
        leftClick(helper, player, CELL);
        check(helper, depths(helper, CELL).equals(new Depths(8, 8, 8, 9)), "hanging: left-click lengthens");
        helper.succeed();
    }

    @CBGameTest
    public void hammerSmoothsMatchingNeighbors(GameTestHelper helper) {
        floor(helper);
        BlockPos east = CELL.east();
        BlockPos north = CELL.north();
        BlockPos northEast = north.east();
        place(helper, CELL, Direction.UP, new Depths(8, 8, 8, 8));
        place(helper, east, Direction.UP, new Depths(8, 8, 8, 8));
        place(helper, northEast, Direction.UP, new Depths(8, 8, 8, 8));
        place(helper, north, Direction.DOWN, new Depths(8, 8, 8, 8));
        rightClick(helper, aim(helper, CELL, 0.75, 0.25), CELL);
        check(helper, depths(helper, CELL).pn() == 9, "the corner rose");
        check(helper, depths(helper, east).nn() == 9, "the east block's meeting corner followed");
        check(helper, depths(helper, northEast).np() == 9, "so did the diagonal block's");
        check(helper, depths(helper, north).equals(new Depths(8, 8, 8, 8)), "a hanging block doesn't");
        helper.succeed();
    }

    @CBGameTest
    public void shapesFollowTheCorners(GameTestHelper helper) {
        floor(helper);
        place(helper, CELL, Direction.UP, new Depths(16, 0, 16, 0));
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(CELL);
        BlockState state = level.getBlockState(abs);
        VoxelShape collision = state.getCollisionShape(level, abs, CollisionContext.empty());
        check(helper, solidAt(collision, 0.25, 0.9, 0.25) && solidAt(collision, 0.75, 0.9, 0.25),
                "full height along the north");
        check(helper, solidAt(collision, 0.25, 0.4, 0.75) && !solidAt(collision, 0.25, 0.6, 0.75),
                "the south half is held to half a block below the top, so it can be walked up");
        check(helper, state.getShape(level, abs).bounds().maxY == 1, "outlined up to the highest corner");
        check(helper, sturdy(helper, CELL, Direction.NORTH) && sturdy(helper, CELL, Direction.DOWN),
                "the full north side and the floor side are whole");
        check(helper, !sturdy(helper, CELL, Direction.SOUTH) && !sturdy(helper, CELL, Direction.UP)
                && !sturdy(helper, CELL, Direction.WEST), "the sloped sides are not");

        place(helper, CELL, Direction.DOWN, new Depths(4, 4, 4, 4));
        state = level.getBlockState(abs);
        collision = state.getCollisionShape(level, abs, CollisionContext.empty());
        check(helper, solidAt(collision, 0.5, 0.9, 0.5) && !solidAt(collision, 0.5, 0.7, 0.5),
                "hanging: a quarter block down from the ceiling");
        check(helper, sturdy(helper, CELL, Direction.UP) && !sturdy(helper, CELL, Direction.DOWN),
                "hanging: the ceiling side is whole");

        // A see-through cover makes no face whole, as the original
        place(helper, CELL, Direction.UP, Depths.FULL);
        be(helper, CELL).setCover(CoverData.BASE, Blocks.GLASS.defaultBlockState());
        check(helper, !sturdy(helper, CELL, Direction.UP), "glass-covered");
        helper.succeed();
    }

    @CBGameTest
    public void dropsItself(GameTestHelper helper) {
        floor(helper);
        place(helper, CELL, Direction.UP, new Depths(3, 5, 7, 9));
        helper.getLevel().destroyBlock(helper.absolutePos(CELL), true);
        helper.assertItemEntityPresent(CollapsibleContent.CARPENTERS_COLLAPSIBLE_BLOCK.asItem(), CELL, 2);
        helper.succeed();
    }
}
