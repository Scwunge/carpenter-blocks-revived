package com.carpentersblocks.test.barrier;

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
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.carpentersblocks.block.barrier.BarrierType;
import com.carpentersblocks.block.barrier.CarpentersBarrierBlock;
import com.carpentersblocks.block.barrier.CarpentersGateBlock;
import com.carpentersblocks.registry.BarrierContent;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 4: Carpenter's Barrier and Gate connections, posts, hammer use, opening and redstone. */
public class BarrierTests {

    private static final BlockPos FLOOR = new BlockPos(1, 0, 1);
    private static final BlockPos ON_FLOOR = FLOOR.above();

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static Player player(GameTestHelper helper, Direction look, ItemStack stack) {
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setYRot(look.toYRot());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return player;
    }

    private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction face) {
        Vec3 onFace = Vec3.atCenterOf(helper.absolutePos(pos)).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5));
        return new BlockHitResult(onFace, face, helper.absolutePos(pos), false);
    }

    /** Places the held block on top of {@code below}, as a player would. */
    private static void placeOnTop(GameTestHelper helper, Player player, BlockPos below) {
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, below, Direction.UP)));
    }

    /** Places a barrier as a player would, returning that player, who owns it. */
    private static Player placeBarrier(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.below(), Blocks.STONE);
        Player player = player(helper, Direction.NORTH, new ItemStack(BarrierContent.CARPENTERS_BARRIER));
        placeOnTop(helper, player, pos.below());
        return player;
    }

    private static boolean joined(GameTestHelper helper, BlockPos pos, Direction side) {
        BlockState state = helper.getBlockState(pos);
        return CarpentersBarrierBlock.connections(state).contains(side);
    }

    @CBGameTest
    public void barriersJoinEachOtherAndSolidBlocks(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 0), Blocks.STONE);
        placeBarrier(helper, new BlockPos(2, 1, 1));
        placeBarrier(helper, ON_FLOOR);
        check(helper, joined(helper, ON_FLOOR, Direction.NORTH), "joins the stone to the north");
        check(helper, joined(helper, ON_FLOOR, Direction.EAST), "joins the barrier to the east");
        check(helper, joined(helper, new BlockPos(2, 1, 1), Direction.WEST), "east barrier joins back");
        check(helper, !joined(helper, ON_FLOOR, Direction.SOUTH), "nothing to the south");
        helper.succeed();
    }

    @CBGameTest
    public void barriersAndVanillaFencesJoin(GameTestHelper helper) {
        BlockPos west = new BlockPos(0, 1, 1);
        helper.setBlock(west, Blocks.OAK_FENCE);
        placeBarrier(helper, ON_FLOOR);
        check(helper, joined(helper, ON_FLOOR, Direction.WEST), "barrier joins the fence");
        check(helper, helper.getBlockState(west).getValue(FenceBlock.EAST), "fence joins the barrier");
        helper.succeed();
    }

    @CBGameTest
    public void forcedPostLetsGoOfSolidBlocks(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 0), Blocks.STONE);
        placeBarrier(helper, new BlockPos(2, 1, 1));
        Player player = placeBarrier(helper, ON_FLOOR);
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(ON_FLOOR);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        BarrierContent.CARPENTERS_BARRIER.attack(level.getBlockState(pos), level, pos, player, Direction.UP);
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersBarrierBlock.POST), "post forced");
        check(helper, !joined(helper, ON_FLOOR, Direction.NORTH), "lets go of the stone");
        check(helper, joined(helper, ON_FLOOR, Direction.EAST), "still joins the barrier");
        helper.succeed();
    }

    @CBGameTest
    public void hammerChangesTheStyle(GameTestHelper helper) {
        Player player = placeBarrier(helper, ON_FLOOR);
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(ON_FLOOR);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        player.setShiftKeyDown(true);
        BarrierContent.CARPENTERS_BARRIER.use(level.getBlockState(pos), level, pos, player, InteractionHand.MAIN_HAND,
                hit(helper, ON_FLOOR, Direction.UP));
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersBarrierBlock.TYPE) == BarrierType.VANILLA_X1,
                "sneaking picks the next vanilla sub-type");
        player.setShiftKeyDown(false);
        BarrierContent.CARPENTERS_BARRIER.use(level.getBlockState(pos), level, pos, player, InteractionHand.MAIN_HAND,
                hit(helper, ON_FLOOR, Direction.UP));
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersBarrierBlock.TYPE) == BarrierType.PICKET,
                "right-click picks the next style");
        helper.succeed();
    }

    @CBGameTest
    public void barriersAreTooTallToJump(GameTestHelper helper) {
        placeBarrier(helper, ON_FLOOR);
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(ON_FLOOR);
        check(helper, level.getBlockState(pos).getCollisionShape(level, pos).bounds().maxY == 1.5, "1.5 blocks tall");
        helper.succeed();
    }

    @CBGameTest
    public void postsShowAtEndsButNotAlongARun(GameTestHelper helper) {
        for (int z = 0; z < 3; ++z) {
            placeBarrier(helper, new BlockPos(1, 1, z));
        }
        Level level = helper.getLevel();
        check(helper, CarpentersBarrierBlock.surroundings(level, helper.absolutePos(new BlockPos(1, 1, 0))).post(),
                "end of the run has a post");
        check(helper, !CarpentersBarrierBlock.surroundings(level, helper.absolutePos(new BlockPos(1, 1, 1))).post(),
                "middle of the run has none");
        helper.succeed();
    }

    @CBGameTest
    public void newGatesMatchTheBarrierBeside(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 1), BarrierContent.CARPENTERS_BARRIER.defaultBlockState()
                .setValue(CarpentersBarrierBlock.TYPE, BarrierType.PICKET));
        helper.setBlock(FLOOR, Blocks.STONE);
        placeOnTop(helper, player(helper, Direction.NORTH, new ItemStack(BarrierContent.CARPENTERS_GATE)), FLOOR);
        BlockState gate = helper.getBlockState(ON_FLOOR);
        check(helper, gate.is(BarrierContent.CARPENTERS_GATE), "gate placed");
        check(helper, gate.getValue(CarpentersGateBlock.TYPE) == BarrierType.PICKET, "picket like its neighbor");
        check(helper, !CarpentersGateBlock.acrossZ(gate), "looking north, the gate runs east-west");
        helper.succeed();
    }

    @CBGameTest
    public void gatesNeedGroundBelow(GameTestHelper helper) {
        BlockPos side = new BlockPos(1, 2, 1);
        helper.setBlock(side, Blocks.STONE);
        Player player = player(helper, Direction.NORTH, new ItemStack(BarrierContent.CARPENTERS_GATE));
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, side,
                Direction.NORTH)));
        check(helper, helper.getBlockState(side.north()).isAir(), "no gate in mid-air");
        helper.succeed();
    }

    @CBGameTest
    public void gatesOpenAwayAndStackedGatesFollow(GameTestHelper helper) {
        BlockState closed = BarrierContent.CARPENTERS_GATE.defaultBlockState();
        helper.setBlock(FLOOR, Blocks.STONE);
        helper.setBlock(ON_FLOOR, closed);
        helper.setBlock(ON_FLOOR.above(), closed);
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(ON_FLOOR);
        Player player = player(helper, Direction.SOUTH, ItemStack.EMPTY);

        BarrierContent.CARPENTERS_GATE.use(level.getBlockState(pos), level, pos, player, InteractionHand.MAIN_HAND,
                hit(helper, ON_FLOOR, Direction.NORTH));
        BlockState open = helper.getBlockState(ON_FLOOR);
        check(helper, open.getValue(CarpentersGateBlock.OPEN), "gate opens");
        check(helper, open.getValue(CarpentersGateBlock.FACING) == Direction.WEST, "swings south, away from the player");
        check(helper, open.getCollisionShape(level, pos).isEmpty(), "open gates let things through");
        check(helper, helper.getBlockState(ON_FLOOR.above()).getValue(CarpentersGateBlock.OPEN), "stacked gate follows");

        BarrierContent.CARPENTERS_GATE.use(level.getBlockState(pos), level, pos, player, InteractionHand.MAIN_HAND,
                hit(helper, ON_FLOOR, Direction.NORTH));
        check(helper, !helper.getBlockState(ON_FLOOR).getValue(CarpentersGateBlock.OPEN), "gate closes");
        check(helper, !helper.getBlockState(ON_FLOOR.above()).getValue(CarpentersGateBlock.OPEN), "and so does the top");
        helper.succeed();
    }

    @CBGameTest
    public void redstoneOpensAndClosesGates(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        helper.setBlock(ON_FLOOR, BarrierContent.CARPENTERS_GATE.defaultBlockState());
        BlockPos power = new BlockPos(1, 1, 0);
        helper.setBlock(power, Blocks.REDSTONE_BLOCK);
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersGateBlock.OPEN), "power opens the gate");
        check(helper, helper.getBlockState(ON_FLOOR).getValue(BlockStateProperties.POWERED), "and marks it powered");
        helper.setBlock(power, Blocks.AIR);
        check(helper, !helper.getBlockState(ON_FLOOR).getValue(CarpentersGateBlock.OPEN), "losing power closes it");
        helper.succeed();
    }
}
