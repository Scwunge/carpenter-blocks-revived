package com.carpentersblocks.test.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.carpentersblocks.block.CarpentersBlockBlock;
import com.carpentersblocks.block.FrameShape;
import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.coverable.Overlay;
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

    @CBGameTest
    public void onlySlabsHoldWater(GameTestHelper helper) {
        Level level = helper.getLevel();
        BlockPos pos = helper.absolutePos(FLOOR);
        CarpentersBlockBlock block = (CarpentersBlockBlock) ModContent.CARPENTERS_BLOCK;
        frame(helper, FLOOR, Blocks.STONE.defaultBlockState());
        check(helper, !block.canPlaceLiquid(null, level, pos, level.getBlockState(pos), Fluids.WATER),
                "full frame must not take water");
        helper.setBlock(FLOOR, level.getBlockState(pos).setValue(CarpentersBlockBlock.SHAPE, FrameShape.DOWN));
        check(helper, block.placeLiquid(level, pos, level.getBlockState(pos), Fluids.WATER.getSource(false)),
                "slab frame takes water");
        helper.assertBlockProperty(FLOOR, CoverableBlock.WATERLOGGED, true);
        check(helper, level.getFluidState(pos).is(Fluids.WATER), "slab frame holds water");
        helper.succeed();
    }

    /** Right-clicks the top of the block at {@code rel} holding {@code stack}, as the loader click events do. */
    private static void rightClick(GameTestHelper helper, BlockPos rel, ItemStack stack) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos pos = helper.absolutePos(rel);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
        block().use(helper.getLevel().getBlockState(pos), helper.getLevel(), pos, player, InteractionHand.MAIN_HAND,
                hit);
    }

    private static BlockState base(GameTestHelper helper, BlockPos rel) {
        CoverableBlockEntity entity = helper.getBlockEntity(rel);
        return entity.getCoverData().cover(CoverData.BASE);
    }

    @CBGameTest
    public void axeStripsLogCover(GameTestHelper helper) {
        frame(helper, FLOOR, Blocks.OAK_LOG.defaultBlockState());
        ItemStack axe = new ItemStack(Items.IRON_AXE);
        rightClick(helper, FLOOR, axe);
        check(helper, base(helper, FLOOR).is(Blocks.STRIPPED_OAK_LOG), "axe strips oak log cover");
        check(helper, axe.getDamageValue() == 1, "stripping wears the axe");
        helper.succeed();
    }

    @CBGameTest
    public void copperCoverWaxAndScrape(GameTestHelper helper) {
        frame(helper, FLOOR, Blocks.EXPOSED_COPPER.defaultBlockState());
        ItemStack honeycomb = new ItemStack(Items.HONEYCOMB, 2);
        rightClick(helper, FLOOR, honeycomb);
        check(helper, base(helper, FLOOR).is(Blocks.WAXED_EXPOSED_COPPER), "honeycomb waxes copper cover");
        check(helper, honeycomb.getCount() == 1, "waxing uses a honeycomb");
        rightClick(helper, FLOOR, new ItemStack(Items.IRON_AXE));
        check(helper, base(helper, FLOOR).is(Blocks.EXPOSED_COPPER), "axe removes wax");
        rightClick(helper, FLOOR, new ItemStack(Items.IRON_AXE));
        check(helper, base(helper, FLOOR).is(Blocks.COPPER_BLOCK), "axe scrapes oxidation");
        helper.succeed();
    }

    @CBGameTest
    public void mossOverlaySupportsFlowers(GameTestHelper helper) {
        CoverableBlockEntity entity = frame(helper, FLOOR, Blocks.STONE.defaultBlockState());
        entity.setOverlay(CoverData.BASE, Overlay.MOSS, new ItemStack(Items.MOSS_CARPET));
        check(helper, Blocks.POPPY.defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(ABOVE)),
                "poppy grows on moss overlay");
        check(helper, Overlay.forItem(new ItemStack(Items.SCULK_VEIN)) == Overlay.SCULK, "sculk vein is an overlay");
        helper.succeed();
    }

    @CBGameTest
    public void commandsReplaceWithoutDrops(GameTestHelper helper) {
        CoverableBlockEntity entity = frame(helper, FLOOR, Blocks.STONE.defaultBlockState());
        entity.setDye(CoverData.BASE, DyeColor.RED, new ItemStack(Items.RED_DYE));
        Clearable.tryClear(entity);
        helper.setBlock(FLOOR, Blocks.AIR);
        helper.assertItemEntityCountIs(Items.STONE, ABOVE, 3.0, 0);
        helper.assertItemEntityCountIs(Items.RED_DYE, ABOVE, 3.0, 0);
        helper.succeed();
    }
}
