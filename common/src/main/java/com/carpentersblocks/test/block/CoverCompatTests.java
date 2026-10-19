package com.carpentersblocks.test.block;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.AttributeItems;
import com.carpentersblocks.block.slope.SlopedCoverFace;
import com.carpentersblocks.test.CBGameTest;

/**
 * Batch 12: what packs and other mods can change about covers. The shipped allow and deny lists are empty, so entries
 * are tried through {@link AttributeItems#coverStateFor} with the lists given; no block from another mod is around in
 * the test servers, so the allow list stands in for one that fails the usual checks.
 */
public class CoverCompatTests {

    private static final BlockHitResult HIT = new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false);
    private static final Predicate<BlockState> NONE = state -> false;

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    @Nullable
    private static BlockState cover(ItemStack stack, Predicate<BlockState> denied, Predicate<BlockState> allowed) {
        return AttributeItems.coverStateFor(stack, null, InteractionHand.MAIN_HAND, HIT, denied, allowed);
    }

    private static Predicate<BlockState> is(Block block) {
        return state -> state.is(block);
    }

    @CBGameTest
    public void coverTagsLoadSoPacksCanExtendThem(GameTestHelper helper) {
        for (TagKey<Block> tag : List.of(AttributeItems.COVER_ALLOWLIST, AttributeItems.COVER_DENYLIST,
                SlopedCoverFace.SLOPE_TOP_COVERS)) {
            check(helper, BuiltInRegistries.BLOCK.getTag(tag).isPresent(), tag.location() + " loaded");
        }
        check(helper, Blocks.DIRT_PATH.defaultBlockState().is(SlopedCoverFace.SLOPE_TOP_COVERS),
                "the shipped slope_top_covers entries are in");
        helper.succeed();
    }

    @CBGameTest
    public void allowListTakesBlocksTheChecksRefuse(GameTestHelper helper) {
        ItemStack lantern = new ItemStack(Items.LANTERN);
        ItemStack chest = new ItemStack(Items.CHEST);
        check(helper, cover(lantern, NONE, NONE) == null, "a lantern isn't a full block");
        check(helper, cover(chest, NONE, NONE) == null, "a chest has a block entity");
        check(helper, Blocks.LANTERN.defaultBlockState().equals(cover(lantern, NONE, is(Blocks.LANTERN))),
                "allow-listed lantern");
        check(helper, cover(chest, NONE, is(Blocks.CHEST)) != null, "allow-listed chest");
        check(helper, cover(chest, is(Blocks.CHEST), is(Blocks.CHEST)) == null, "the deny list wins");
        helper.succeed();
    }

    @CBGameTest
    public void denyListRefusesAGoodCover(GameTestHelper helper) {
        ItemStack stone = new ItemStack(Items.STONE);
        check(helper, Blocks.STONE.defaultBlockState().equals(cover(stone, NONE, NONE)), "stone covers");
        check(helper, cover(stone, is(Blocks.STONE), NONE) == null, "deny-listed stone doesn't");
        check(helper, AttributeItems.coverStateFor(stone, null, InteractionHand.MAIN_HAND, HIT) != null,
                "the shipped deny list is empty");
        helper.succeed();
    }

    @CBGameTest
    public void slabsCoverAsDoubleSlabsAndStairsDont(GameTestHelper helper) {
        BlockState slab = cover(new ItemStack(Items.SMOOTH_STONE_SLAB), NONE, NONE);
        check(helper, slab != null && slab.getValue(SlabBlock.TYPE) == SlabType.DOUBLE, "double slab: " + slab);
        check(helper, cover(new ItemStack(Items.OAK_STAIRS), NONE, NONE) == null, "stairs aren't full");
        helper.succeed();
    }

    @CBGameTest
    public void slopeTopCoversShowTheirTopOnUpwardSlopes(GameTestHelper helper) {
        // Dirt path is a not-quite-full block in the tag
        BlockState path = Blocks.DIRT_PATH.defaultBlockState();
        check(helper, SlopedCoverFace.of(path, true, Direction.NORTH, true) == Direction.UP, "path up");
        check(helper, SlopedCoverFace.of(path, false, Direction.NORTH, true) == Direction.NORTH, "path down: side");
        BlockState slab = Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
        check(helper, SlopedCoverFace.of(slab, true, Direction.EAST, true) == Direction.EAST,
                "a slab outside the tag shows its side");
        check(helper, SlopedCoverFace.of(Blocks.GRASS_BLOCK.defaultBlockState(), true, Direction.SOUTH, true)
                == Direction.UP, "grass up");
        check(helper, SlopedCoverFace.of(Blocks.OAK_LOG.defaultBlockState(), true, Direction.SOUTH, true)
                == Direction.UP, "upright log: its end");
        helper.succeed();
    }
}
