package com.carpentersblocks.test.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 1: properties a Carpenter's Block borrows from its covers. */
public class CoverBehaviorTests {

    private static final BlockPos FLOOR = new BlockPos(1, 0, 1);
    private static final BlockPos ABOVE = FLOOR.above();

    private static CoverableBlockEntity frame(GameTestHelper helper, BlockPos pos, BlockState base) {
        helper.setBlock(pos, ModContent.CARPENTERS_BLOCK);
        CoverableBlockEntity entity = helper.getBlockEntity(pos);
        if (base != null) {
            entity.setCover(CoverData.BASE, base);
        }
        return entity;
    }

    private static CoverableBlock block() {
        return (CoverableBlock) ModContent.CARPENTERS_BLOCK;
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    @CBGameTest
    public void bareFrameBurnsLikeTheOriginal(GameTestHelper helper) {
        frame(helper, FLOOR, null);
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(FLOOR);
        BlockState state = level.getBlockState(pos);
        check(helper, block().getFlammability(state, level, pos, Direction.UP) == CoverableBlock.FRAME_FLAMMABILITY,
                "bare frame flammability");
        check(helper, block().getFireSpreadSpeed(state, level, pos, Direction.UP) == CoverableBlock.FRAME_FIRE_SPREAD,
                "bare frame fire spread");
        helper.succeed();
    }

    @CBGameTest
    public void coverDecidesFlammability(GameTestHelper helper) {
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(FLOOR);
        frame(helper, FLOOR, Blocks.STONE.defaultBlockState());
        check(helper, block().getFlammability(level.getBlockState(pos), level, pos, Direction.UP) == 0,
                "stone cover must not burn");
        frame(helper, FLOOR, Blocks.WHITE_WOOL.defaultBlockState());
        check(helper, block().getFlammability(level.getBlockState(pos), level, pos, Direction.UP) == 60,
                "wool cover burns like wool");
        check(helper, block().getFireSpreadSpeed(level.getBlockState(pos), level, pos, Direction.UP) == 30,
                "wool cover catches like wool");
        helper.succeed();
    }

    @CBGameTest
    public void netherrackCoverIsAFireSource(GameTestHelper helper) {
        frame(helper, FLOOR, Blocks.NETHERRACK.defaultBlockState());
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(FLOOR);
        check(helper, block().isFireSource(level.getBlockState(pos), level, pos, Direction.UP),
                "netherrack cover burns forever");
        helper.succeed();
    }

    @CBGameTest
    public void soundAndFrictionFollowCover(GameTestHelper helper) {
        frame(helper, FLOOR, Blocks.ICE.defaultBlockState());
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(FLOOR);
        BlockState state = level.getBlockState(pos);
        check(helper, block().getSoundType(state, level, pos, null) == SoundType.GLASS, "ice cover sounds like ice");
        check(helper, block().getFriction(state, level, pos, null) == Blocks.ICE.getFriction(),
                "ice cover is slippery");
        helper.succeed();
    }

    @CBGameTest
    public void flowersNeedSoilCover(GameTestHelper helper) {
        frame(helper, FLOOR, Blocks.STONE.defaultBlockState());
        Level level = helper.getLevel();
        BlockPos plant = helper.absolutePos(ABOVE);
        check(helper, !Blocks.POPPY.defaultBlockState().canSurvive(level, plant), "poppy must not grow on stone");
        frame(helper, FLOOR, Blocks.DIRT.defaultBlockState());
        check(helper, Blocks.POPPY.defaultBlockState().canSurvive(level, plant), "poppy grows on dirt cover");
        check(helper, Blocks.OAK_SAPLING.defaultBlockState().canSurvive(level, plant), "sapling grows on dirt cover");
        check(helper, !Blocks.WHEAT.defaultBlockState().canSurvive(level, plant), "wheat still needs farmland");
        helper.succeed();
    }

    @CBGameTest
    public void desertAndNetherPlants(GameTestHelper helper) {
        Level level = helper.getLevel();
        BlockPos plant = helper.absolutePos(ABOVE);
        frame(helper, FLOOR, Blocks.SAND.defaultBlockState());
        check(helper, Blocks.CACTUS.defaultBlockState().canSurvive(level, plant), "cactus grows on sand cover");
        check(helper, Blocks.DEAD_BUSH.defaultBlockState().canSurvive(level, plant), "dead bush grows on sand cover");
        check(helper, !Blocks.NETHER_WART.defaultBlockState().canSurvive(level, plant), "wart needs soul sand");
        frame(helper, FLOOR, Blocks.SOUL_SAND.defaultBlockState());
        check(helper, Blocks.NETHER_WART.defaultBlockState().canSurvive(level, plant), "wart grows on soul sand cover");
        helper.succeed();
    }

    @CBGameTest
    public void sugarCaneNeedsWater(GameTestHelper helper) {
        Level level = helper.getLevel();
        BlockPos plant = helper.absolutePos(ABOVE);
        frame(helper, FLOOR, Blocks.SAND.defaultBlockState());
        check(helper, !Blocks.SUGAR_CANE.defaultBlockState().canSurvive(level, plant), "cane needs water");
        helper.setBlock(FLOOR.west(), Blocks.WATER);
        check(helper, Blocks.SUGAR_CANE.defaultBlockState().canSurvive(level, plant), "cane grows beside water");
        helper.succeed();
    }

    @CBGameTest
    public void sideCoverPopsOffAgainstSolidNeighbor(GameTestHelper helper) {
        CoverableBlockEntity entity = frame(helper, ABOVE, Blocks.STONE.defaultBlockState());
        int north = Direction.NORTH.get3DDataValue();
        entity.setCover(north, Blocks.OAK_PLANKS.defaultBlockState());
        check(helper, entity.getCoverData().hasCover(north), "side cover applied");
        helper.setBlock(ABOVE.north(), Blocks.STONE);
        check(helper, !entity.getCoverData().hasCover(north), "side cover ejected against stone");
        helper.succeed();
    }

    @CBGameTest
    public void illuminatorAndGlowingCoversLight(GameTestHelper helper) {
        CoverableBlockEntity entity = frame(helper, FLOOR, Blocks.STONE.defaultBlockState());
        helper.assertBlockProperty(FLOOR, CoverableBlock.LIGHT, 0);
        entity.setIlluminator(new ItemStack(Items.GLOWSTONE_DUST));
        helper.assertBlockProperty(FLOOR, CoverableBlock.LIGHT, 15);
        frame(helper, ABOVE, Blocks.SEA_LANTERN.defaultBlockState());
        helper.assertBlockProperty(ABOVE, CoverableBlock.LIGHT, 15);
        helper.succeed();
    }
}
