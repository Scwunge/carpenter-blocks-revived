package com.carpentersblocks.test.flowerpot;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.flowerpot.CarpentersFlowerPotBlock;
import com.carpentersblocks.block.flowerpot.FlowerPotBlockEntity;
import com.carpentersblocks.block.flowerpot.PotContents;
import com.carpentersblocks.block.flowerpot.PotGeometry;
import com.carpentersblocks.registry.FlowerPotContent;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.test.CBGameTest;

/** Batch 8: Carpenter's Flower Pot support, soil, plants, shapes, designs, fertilizer and drops. */
public class FlowerPotTests {

    private static final BlockPos FLOOR = new BlockPos(1, 0, 1);
    private static final BlockPos POT = FLOOR.above();

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static Player placePot(GameTestHelper helper, GameType mode) {
        helper.setBlock(FLOOR, Blocks.STONE);
        helper.setBlock(POT, FlowerPotContent.CARPENTERS_FLOWER_POT);
        return helper.makeMockPlayer(mode);
    }

    private static FlowerPotBlockEntity pot(GameTestHelper helper) {
        return (FlowerPotBlockEntity) helper.getBlockEntity(POT);
    }

    /** The middle of the soil area on top, or the middle of a side. */
    private static BlockHitResult hit(GameTestHelper helper, Direction face) {
        Vec3 center = Vec3.atBottomCenterOf(helper.absolutePos(POT));
        Vec3 at = face == Direction.UP ? center.add(0, PotGeometry.SOIL.y2() / 16, 0)
                : center.add(Vec3.atLowerCornerOf(face.getNormal()).scale(3.0 / 16)).add(0, 3.0 / 16, 0);
        return new BlockHitResult(at, face, helper.absolutePos(POT), false);
    }

    private static void use(GameTestHelper helper, Player player, Item item, Direction face) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item, 4));
        Level level = helper.getLevel();
        BlockPos abs = helper.absolutePos(POT);
        FlowerPotContent.CARPENTERS_FLOWER_POT.use(level.getBlockState(abs), level, abs, player,
                InteractionHand.MAIN_HAND, hit(helper, face));
    }

    /** Sneak left-click with the hammer, looking straight down on the pot from above. */
    private static void hammerFromAbove(GameTestHelper helper, Player player) {
        BlockPos abs = helper.absolutePos(POT);
        player.moveTo(abs.getX() + 0.5, abs.getY() + 1.0, abs.getZ() + 0.5, 0, 90);
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModContent.CARPENTERS_HAMMER));
        Level level = helper.getLevel();
        FlowerPotContent.CARPENTERS_FLOWER_POT.attack(level.getBlockState(abs), level, abs, player, Direction.UP);
        player.setShiftKeyDown(false);
    }

    @CBGameTest
    public void potsFallWithoutSupport(GameTestHelper helper) {
        placePot(helper, GameType.CREATIVE);
        helper.setBlock(FLOOR, Blocks.AIR);
        check(helper, helper.getBlockState(POT).isAir(), "nothing to stand on");
        helper.succeed();
    }

    @CBGameTest
    public void soilThenPlant(GameTestHelper helper) {
        Player player = placePot(helper, GameType.SURVIVAL);
        use(helper, player, Items.POPPY, Direction.UP);
        check(helper, !pot(helper).hasPlant(), "no plant without soil");
        use(helper, player, Items.DIRT, Direction.UP);
        check(helper, pot(helper).hasSoil(), "dirt on the soil area is soil");
        check(helper, player.getMainHandItem().getCount() == 3, "one dirt used");
        check(helper, !pot(helper).getCoverData().hasCover(CoverData.BASE), "not a cover");
        player.setYRot(90);
        use(helper, player, Items.POPPY, Direction.UP);
        check(helper, pot(helper).getPlant().is(Items.POPPY), "the poppy is planted");
        check(helper, pot(helper).getAngle() == PotGeometry.angle(90), "turned the way the player looked");
        check(helper, helper.getBlockState(POT).getValue(CarpentersFlowerPotBlock.CONTENTS) == PotContents.PLANT,
                "the outline grows");
        helper.succeed();
    }

    @CBGameTest
    public void soilOffTheSoilAreaCoversThePot(GameTestHelper helper) {
        Player player = placePot(helper, GameType.SURVIVAL);
        use(helper, player, Items.DIRT, Direction.NORTH);
        check(helper, pot(helper).getCoverData().hasCover(CoverData.BASE) && !pot(helper).hasSoil(),
                "on the side: a cover");
        use(helper, player, Items.SAND, Direction.NORTH);
        check(helper, pot(helper).getSoil().is(Items.SAND), "once covered, soil goes in anywhere");
        helper.succeed();
    }

    @CBGameTest
    public void cactusIsSolid(GameTestHelper helper) {
        Player player = placePot(helper, GameType.CREATIVE);
        use(helper, player, Items.SAND, Direction.UP);
        use(helper, player, Items.CACTUS, Direction.UP);
        check(helper, helper.getBlockState(POT).getValue(CarpentersFlowerPotBlock.CONTENTS) == PotContents.SOLID,
                "cactus is solid");
        double top = helper.getBlockState(POT).getCollisionShape(helper.getLevel(), helper.absolutePos(POT),
                CollisionContext.empty()).max(Direction.Axis.Y);
        check(helper, top > 0.9, "and collides up high: " + top);
        helper.succeed();
    }

    @CBGameTest
    public void sneakHammerTakesOutPlantThenSoil(GameTestHelper helper) {
        Player player = placePot(helper, GameType.CREATIVE);
        use(helper, player, Items.DIRT, Direction.UP);
        use(helper, player, Items.FERN, Direction.UP);
        hammerFromAbove(helper, player);
        check(helper, !pot(helper).hasPlant() && pot(helper).hasSoil(), "the plant first");
        hammerFromAbove(helper, player);
        check(helper, !pot(helper).hasSoil(), "then the soil");
        helper.assertItemEntityPresent(Items.FERN, POT, 2);
        helper.assertItemEntityPresent(Items.DIRT, POT, 2);
        helper.succeed();
    }

    @CBGameTest
    public void designsReplaceTheCover(GameTestHelper helper) {
        Player player = placePot(helper, GameType.CREATIVE);
        use(helper, player, Items.OAK_PLANKS, Direction.NORTH);
        check(helper, pot(helper).getCoverData().hasCover(CoverData.BASE), "covered");
        use(helper, player, ModContent.CARPENTERS_HAMMER, Direction.NORTH);
        check(helper, pot(helper).getDesign().equals("black"), "right-click: first design");
        check(helper, !pot(helper).getCoverData().hasCover(CoverData.BASE), "the cover comes off");
        helper.assertItemEntityPresent(Items.OAK_PLANKS, POT, 2);
        use(helper, player, Items.OAK_PLANKS, Direction.NORTH);
        check(helper, !pot(helper).getCoverData().hasCover(CoverData.BASE), "no cover over a design");
        helper.succeed();
    }

    @CBGameTest
    public void fertilizerEnrichesAPlant(GameTestHelper helper) {
        Player player = placePot(helper, GameType.SURVIVAL);
        use(helper, player, Items.DIRT, Direction.UP);
        use(helper, player, Items.FERN, Direction.UP);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE_MEAL, 2));
        CarpentersFlowerPotBlock.enrich(helper.getLevel(), helper.absolutePos(POT), player);
        check(helper, pot(helper).isFertilized(), "fertilized");
        check(helper, player.getMainHandItem().getCount() == 1, "one bone meal used");
        CarpentersFlowerPotBlock.enrich(helper.getLevel(), helper.absolutePos(POT), player);
        check(helper, player.getMainHandItem().getCount() == 1, "only once");
        helper.succeed();
    }

    @CBGameTest
    public void tallPlantsComeOutUnderABlock(GameTestHelper helper) {
        Player player = placePot(helper, GameType.CREATIVE);
        use(helper, player, Items.DIRT, Direction.UP);
        use(helper, player, Items.TALL_GRASS, Direction.UP);
        check(helper, pot(helper).hasPlant(), "tall grass planted");
        helper.setBlock(POT.above(), Blocks.STONE);
        check(helper, !pot(helper).hasPlant(), "pushed out by the block above");
        helper.assertItemEntityPresent(Items.TALL_GRASS, POT, 2);
        helper.succeed();
    }

    @CBGameTest
    public void breakingDropsEverything(GameTestHelper helper) {
        Player player = placePot(helper, GameType.CREATIVE);
        use(helper, player, Items.DIRT, Direction.UP);
        use(helper, player, Items.FERN, Direction.UP);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE_MEAL));
        CarpentersFlowerPotBlock.enrich(helper.getLevel(), helper.absolutePos(POT), player);
        helper.getLevel().destroyBlock(helper.absolutePos(POT), true);
        helper.assertItemEntityPresent(FlowerPotContent.CARPENTERS_FLOWER_POT.asItem(), POT, 2);
        helper.assertItemEntityPresent(Items.DIRT, POT, 2);
        helper.assertItemEntityPresent(Items.FERN, POT, 2);
        helper.assertItemEntityPresent(Items.BONE_MEAL, POT, 2);
        helper.succeed();
    }
}
