package com.carpentersblocks.test.stairs;

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

import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.stairs.CarpentersStairsBlock;
import com.carpentersblocks.block.stairs.StairsShape;
import com.carpentersblocks.config.CBConfig;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.registry.StairsContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 3: Carpenter's Stairs placement, joining, hammer use and covers. */
public class StairsTests {

    private static final BlockPos FLOOR = new BlockPos(1, 0, 1);
    private static final BlockPos ON_FLOOR = FLOOR.above();

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static StairsShape shapeAt(GameTestHelper helper, BlockPos pos) {
        BlockState state = helper.getBlockState(pos);
        if (!(state.getBlock() instanceof CarpentersStairsBlock)) {
            helper.fail("no stairs at " + pos + ": " + state);
        }
        return state.getValue(CarpentersStairsBlock.SHAPE);
    }

    private static void setStairs(GameTestHelper helper, BlockPos pos, StairsShape shape) {
        helper.setBlock(pos, StairsContent.CARPENTERS_STAIRS.defaultBlockState()
                .setValue(CarpentersStairsBlock.SHAPE, shape));
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

    private static ItemStack stairsItem() {
        return new ItemStack(StairsContent.CARPENTERS_STAIRS);
    }

    @CBGameTest
    public void placedOnTopClimbsTheWayThePlayerLooks(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        useItemOn(helper, player(helper, GameType.CREATIVE, Direction.SOUTH, stairsItem()), FLOOR, Direction.UP);
        check(helper, shapeAt(helper, ON_FLOOR) == StairsShape.POS_N, "expected upright stairs open to the north");
        helper.succeed();
    }

    @CBGameTest
    public void placedUnderACeilingIsUpsideDown(GameTestHelper helper) {
        BlockPos ceiling = new BlockPos(1, 2, 1);
        helper.setBlock(ceiling, Blocks.STONE);
        useItemOn(helper, player(helper, GameType.CREATIVE, Direction.WEST, stairsItem()), ceiling, Direction.DOWN);
        check(helper, shapeAt(helper, ceiling.below()) == StairsShape.NEG_E, "expected upside-down stairs");
        helper.succeed();
    }

    @CBGameTest
    public void placingBesideStairsTurnsThemIntoACorner(GameTestHelper helper) {
        BlockPos north = new BlockPos(1, 1, 0);
        setStairs(helper, north, StairsShape.POS_N);
        helper.setBlock(FLOOR, Blocks.STONE);
        useItemOn(helper, player(helper, GameType.CREATIVE, Direction.EAST, stairsItem()), FLOOR, Direction.UP);
        check(helper, shapeAt(helper, ON_FLOOR) == StairsShape.POS_W, "placed stairs keep their shape");
        check(helper, shapeAt(helper, north) == StairsShape.EXT_POS_NW, "neighbor becomes an exterior corner");
        helper.succeed();
    }

    @CBGameTest
    public void sneakingSkipsJoining(GameTestHelper helper) {
        BlockPos north = new BlockPos(1, 1, 0);
        setStairs(helper, north, StairsShape.POS_N);
        helper.setBlock(FLOOR, Blocks.STONE);
        Player player = player(helper, GameType.CREATIVE, Direction.EAST, stairsItem());
        player.setShiftKeyDown(true);
        useItemOn(helper, player, FLOOR, Direction.UP);
        check(helper, shapeAt(helper, ON_FLOOR) == StairsShape.POS_W, "placed stairs");
        check(helper, shapeAt(helper, north) == StairsShape.POS_N, "neighbor must stay as it was");
        helper.succeed();
    }

    @CBGameTest
    public void placedInWaterIsWaterlogged(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE);
        helper.setBlock(ON_FLOOR, Blocks.WATER);
        useItemOn(helper, player(helper, GameType.CREATIVE, Direction.NORTH, stairsItem()), FLOOR, Direction.UP);
        shapeAt(helper, ON_FLOOR);
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersStairsBlock.WATERLOGGED), "not waterlogged");
        helper.succeed();
    }

    @CBGameTest
    public void hammerTurnsAndChangesStairs(GameTestHelper helper) {
        setStairs(helper, ON_FLOOR, StairsShape.POS_N);
        Player player = player(helper, GameType.CREATIVE, Direction.SOUTH, new ItemStack(ModContent.CARPENTERS_HAMMER));
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(ON_FLOOR);

        StairsContent.CARPENTERS_STAIRS.attack(level.getBlockState(pos), level, pos, player, Direction.UP);
        check(helper, shapeAt(helper, ON_FLOOR) == StairsShape.POS_S, "left-click turns the stairs");

        StairsContent.CARPENTERS_STAIRS.use(level.getBlockState(pos), level, pos, player, InteractionHand.MAIN_HAND,
                hit(helper, ON_FLOOR, Direction.UP));
        check(helper, shapeAt(helper, ON_FLOOR) == StairsShape.NEG_S, "right-click turns them upside down");
        helper.succeed();
    }

    @CBGameTest
    public void hammerDamageFollowsTheStairsChance(GameTestHelper helper) {
        double chance = CBConfig.itemHammerDamageChanceFromStairs;
        try {
            setStairs(helper, ON_FLOOR, StairsShape.POS_N);
            Level level = helper.getLevel();
            BlockPos pos = helper.absolutePos(ON_FLOOR);
            ItemStack hammer = new ItemStack(ModContent.CARPENTERS_HAMMER);
            Player player = player(helper, GameType.SURVIVAL, Direction.SOUTH, hammer);

            CBConfig.itemHammerDamageChanceFromStairs = 0.0;
            StairsContent.CARPENTERS_STAIRS.use(level.getBlockState(pos), level, pos, player,
                    InteractionHand.MAIN_HAND, hit(helper, ON_FLOOR, Direction.UP));
            check(helper, player.getMainHandItem().getDamageValue() == 0, "chance 0 must not wear the hammer");

            CBConfig.itemHammerDamageChanceFromStairs = 1.0;
            StairsContent.CARPENTERS_STAIRS.use(level.getBlockState(pos), level, pos, player,
                    InteractionHand.MAIN_HAND, hit(helper, ON_FLOOR, Direction.UP));
            check(helper, !CBConfig.toolsDamageable || player.getMainHandItem().getDamageValue() == 1,
                    "chance 1 wears the hammer");
        } finally {
            CBConfig.itemHammerDamageChanceFromStairs = chance;
        }
        helper.succeed();
    }

    @CBGameTest
    public void sidesTakeCoversAfterTheBase(GameTestHelper helper) {
        setStairs(helper, ON_FLOOR, StairsShape.POS_N);
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(ON_FLOOR);
        Player player = player(helper, GameType.CREATIVE, Direction.SOUTH, new ItemStack(Blocks.STONE, 2));
        for (int i = 0; i < 2; ++i) {
            StairsContent.CARPENTERS_STAIRS.use(level.getBlockState(pos), level, pos, player,
                    InteractionHand.MAIN_HAND, hit(helper, ON_FLOOR, Direction.SOUTH));
        }
        CoverData data = ((CoverableBlockEntity) helper.getBlockEntity(ON_FLOOR)).getCoverData();
        check(helper, data.hasCover(CoverData.BASE), "first cover goes on the base");
        check(helper, data.hasCover(Direction.SOUTH.get3DDataValue()), "second cover goes on the clicked side");
        helper.succeed();
    }

    @CBGameTest
    public void opaqueCoversMakeStairsSolid(GameTestHelper helper) {
        setStairs(helper, ON_FLOOR, StairsShape.POS_N);
        CoverableBlockEntity be = helper.getBlockEntity(ON_FLOOR);
        check(helper, !helper.getBlockState(ON_FLOOR).getValue(CarpentersStairsBlock.SOLID), "bare frame is not solid");
        be.setCover(CoverData.BASE, Blocks.STONE.defaultBlockState());
        check(helper, helper.getBlockState(ON_FLOOR).getValue(CarpentersStairsBlock.SOLID), "stone stairs are solid");
        be.setCover(Direction.NORTH.get3DDataValue(), Blocks.GLASS.defaultBlockState());
        check(helper, !helper.getBlockState(ON_FLOOR).getValue(CarpentersStairsBlock.SOLID),
                "a glass side is see-through");
        helper.succeed();
    }

    @CBGameTest
    public void collisionFollowsTheShape(GameTestHelper helper) {
        setStairs(helper, ON_FLOOR, StairsShape.POS_N);
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(ON_FLOOR);
        BlockState state = level.getBlockState(pos);
        check(helper, state.isFaceSturdy(level, pos, Direction.DOWN), "bottom is a full face");
        check(helper, state.isFaceSturdy(level, pos, Direction.SOUTH), "back is a full face");
        check(helper, !state.isFaceSturdy(level, pos, Direction.UP), "top is stepped");
        check(helper, state.getCollisionShape(level, pos).bounds().maxY == 1.0, "stairs are a full block tall");
        helper.succeed();
    }
}
