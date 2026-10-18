package com.carpentersblocks.test.ladder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.carpentersblocks.block.ladder.CarpentersLadderBlock;
import com.carpentersblocks.block.ladder.LadderGeometry;
import com.carpentersblocks.block.ladder.LadderType;
import com.carpentersblocks.block.sided.SidedBlock;
import com.carpentersblocks.registry.LadderContent;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 6: Carpenter's Ladder placement, support, copying, joining, styles and climbing. */
public class LadderTests {

    private static final BlockPos FLOOR = new BlockPos(1, 0, 1);
    private static final BlockPos ON_FLOOR = FLOOR.above();

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction face) {
        Vec3 onFace = Vec3.atCenterOf(helper.absolutePos(pos)).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5));
        return new BlockHitResult(onFace, face, helper.absolutePos(pos), false);
    }

    private static Player place(GameTestHelper helper, BlockPos against, Direction face, Direction look,
            boolean sneaking) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setYRot(look.toYRot());
        player.setShiftKeyDown(sneaking);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(LadderContent.CARPENTERS_LADDER));
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, against, face)));
        player.setShiftKeyDown(false);
        return player;
    }

    @CBGameTest
    public void wallLaddersFallWithTheirWall(GameTestHelper helper) {
        BlockPos wall = new BlockPos(1, 1, 0);
        helper.setBlock(wall, Blocks.STONE);
        place(helper, wall, Direction.SOUTH, Direction.NORTH, false);
        BlockPos ladder = wall.south();
        check(helper, helper.getBlockState(ladder).getValue(SidedBlock.FACING) == Direction.SOUTH, "on the wall");
        check(helper, helper.getBlockState(ladder).is(BlockTags.CLIMBABLE), "climbable");
        helper.setBlock(wall, Blocks.AIR);
        check(helper, helper.getBlockState(ladder).isAir(), "falls without its wall");
        helper.succeed();
    }

    @CBGameTest
    public void freestandingLaddersFaceThePlayerAndStackOnEachOther(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        place(helper, FLOOR, Direction.UP, Direction.EAST, false);
        check(helper, helper.getBlockState(ON_FLOOR).getValue(SidedBlock.FACING) == Direction.UP,
                "looking along x, it runs along z");
        // A ladder's top holds the next one, which follows it whichever way the player looks
        place(helper, ON_FLOOR, Direction.UP, Direction.NORTH, false);
        BlockPos second = ON_FLOOR.above();
        check(helper, helper.getBlockState(second).is(LadderContent.CARPENTERS_LADDER), "stacked on the ladder");
        check(helper, helper.getBlockState(second).getValue(SidedBlock.FACING) == Direction.UP, "follows the one below");
        // Sneaking places it the player's own way
        place(helper, second, Direction.UP, Direction.NORTH, true);
        check(helper, helper.getBlockState(second.above()).getValue(SidedBlock.FACING) == Direction.DOWN,
                "sneaking: runs along x");
        helper.setBlock(FLOOR, Blocks.AIR);
        check(helper, helper.getBlockState(ON_FLOOR).is(LadderContent.CARPENTERS_LADDER), "freestanding stays");
        helper.succeed();
    }

    @CBGameTest
    public void laddersCopyTheStyleBelowUnlessSneaking(GameTestHelper helper) {
        for (int y = 1; y <= 3; ++y) {
            helper.setBlock(new BlockPos(1, y, 0), Blocks.STONE);
        }
        Player player = place(helper, new BlockPos(1, 1, 0), Direction.SOUTH, Direction.NORTH, false);
        BlockPos bottom = new BlockPos(1, 1, 1);
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(bottom);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        LadderContent.CARPENTERS_LADDER.attack(level.getBlockState(abs), level, abs, player, Direction.SOUTH);
        check(helper, helper.getBlockState(bottom).getValue(CarpentersLadderBlock.TYPE) == LadderType.RAIL,
                "left-click: next style");
        place(helper, new BlockPos(1, 2, 0), Direction.SOUTH, Direction.NORTH, false);
        check(helper, helper.getBlockState(bottom.above()).getValue(CarpentersLadderBlock.TYPE) == LadderType.RAIL,
                "copies the style below");
        place(helper, new BlockPos(1, 3, 0), Direction.SOUTH, Direction.NORTH, true);
        check(helper, helper.getBlockState(bottom.above(2)).getValue(CarpentersLadderBlock.TYPE)
                == LadderType.DEFAULT, "sneaking keeps the default");
        LadderContent.CARPENTERS_LADDER.use(level.getBlockState(abs), level, abs, player, InteractionHand.MAIN_HAND,
                hit(helper, bottom, Direction.SOUTH));
        check(helper, helper.getBlockState(bottom).getValue(CarpentersLadderBlock.TYPE) == LadderType.DEFAULT,
                "right-click: previous style");
        helper.succeed();
    }

    @CBGameTest
    public void laddersInARowJoin(GameTestHelper helper) {
        for (int x = 0; x < 3; ++x) {
            helper.setBlock(new BlockPos(x, 1, 0), Blocks.STONE);
            place(helper, new BlockPos(x, 1, 0), Direction.SOUTH, Direction.NORTH, false);
        }
        Level level = helper.getLevel();
        BlockPos middle = helper.absolutePos(new BlockPos(1, 1, 1));
        LadderGeometry.Joins joins = CarpentersLadderBlock.joins(level, middle, level.getBlockState(middle));
        check(helper, joins.low() && joins.high(), "the middle joins both sides");
        BlockPos end = helper.absolutePos(new BlockPos(0, 1, 1));
        LadderGeometry.Joins endJoins = CarpentersLadderBlock.joins(level, end, level.getBlockState(end));
        check(helper, !endJoins.low() && endJoins.high(), "the end joins one side");
        helper.succeed();
    }
}
