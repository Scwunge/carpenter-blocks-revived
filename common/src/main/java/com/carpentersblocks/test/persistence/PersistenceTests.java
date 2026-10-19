package com.carpentersblocks.test.persistence;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.carpentersblocks.block.collapsible.CarpentersCollapsibleBlock;
import com.carpentersblocks.block.collapsible.CollapsibleGeometry.Depths;
import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.coverable.Overlay;
import com.carpentersblocks.block.flowerpot.FlowerPotBlockEntity;
import com.carpentersblocks.block.safe.SafeAutomation;
import com.carpentersblocks.block.safe.SafeBlockEntity;
import com.carpentersblocks.block.slope.CarpentersSlopeBlock;
import com.carpentersblocks.block.slope.SlopeItem;
import com.carpentersblocks.block.slope.SlopeKind;
import com.carpentersblocks.entity.CarpentersTileEntity;
import com.carpentersblocks.registry.BedContent;
import com.carpentersblocks.registry.CollapsibleContent;
import com.carpentersblocks.registry.FlowerPotContent;
import com.carpentersblocks.registry.ModContent;
import com.carpentersblocks.registry.SafeContent;
import com.carpentersblocks.registry.SlopeContent;
import com.carpentersblocks.test.CBGameTest;

/**
 * Batch 12: block entity data survives saving and loading, commands and Ctrl pick-block. Each family's data is saved
 * with {@code saveWithFullMetadata} and loaded into a fresh block entity, as a chunk save and reload would.
 */
public class PersistenceTests {

    private static final BlockPos POS = new BlockPos(1, 1, 1);
    private static final UUID OWNER = UUID.fromString("0c3a9a4e-2b44-4b8e-9d0b-6a1f8f3e2c11");

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static HolderLookup.Provider registries(GameTestHelper helper) {
        return helper.getLevel().registryAccess();
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> T reload(GameTestHelper helper, T be) {
        CompoundTag tag = be.saveWithFullMetadata(registries(helper));
        BlockEntity loaded = BlockEntity.loadStatic(be.getBlockPos(), be.getBlockState(), tag, registries(helper));
        if (loaded == null || loaded == be || loaded.getClass() != be.getClass()) {
            helper.fail("didn't load a fresh " + be.getClass().getSimpleName());
        }
        return (T) loaded;
    }

    private static boolean sameStacks(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); ++i) {
            if (!ItemStack.matches(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    /** Covers, attributes, design, owner and data match, including the items each attribute gives back. */
    private static void checkSame(GameTestHelper helper, CoverableBlockEntity loaded, CoverableBlockEntity original,
            String what) {
        check(helper, loaded.getCoverData().equals(original.getCoverData()), what + ": cover data");
        check(helper, sameStacks(loaded.getAttributeDrops(), original.getAttributeDrops()), what + ": attribute items");
        check(helper, original.getOwner() == null ? loaded.getOwner() == null
                : original.getOwner().equals(loaded.getOwner()), what + ": owner");
        check(helper, loaded.getDesign().equals(original.getDesign()), what + ": design");
        check(helper, loaded.getBlockData() == original.getBlockData(), what + ": block data");
    }

    private static <T extends BlockEntity> T place(GameTestHelper helper, BlockState state, Class<T> type) {
        helper.setBlock(POS.below(), Blocks.STONE);
        helper.setBlock(POS, state);
        return type.cast(helper.getBlockEntity(POS));
    }

    /** Every attribute a coverable block can hold, on the base and a side. */
    private static void decorate(CoverableBlockEntity be) {
        be.setCover(CoverData.BASE, Blocks.OAK_PLANKS.defaultBlockState());
        be.setCover(Direction.NORTH.get3DDataValue(), Blocks.STONE.defaultBlockState());
        be.setDye(CoverData.BASE, DyeColor.RED, new ItemStack(Items.RED_DYE));
        be.setOverlay(CoverData.BASE, Overlay.GRASS, new ItemStack(Items.WHEAT_SEEDS));
        be.setChiselDesign(Direction.NORTH.get3DDataValue(), "brick");
        be.setIlluminator(new ItemStack(Items.GLOWSTONE_DUST));
        be.setOwner(OWNER);
    }

    // Save and load

    @CBGameTest
    public void coverableBlockKeepsEveryAttribute(GameTestHelper helper) {
        CoverableBlockEntity be = place(helper, ModContent.CARPENTERS_BLOCK.defaultBlockState(),
                CoverableBlockEntity.class);
        decorate(be);
        check(helper, be.getCoverData().isIlluminated() && be.getCoverData().hasCover(Direction.NORTH.get3DDataValue()),
                "set up");
        checkSame(helper, reload(helper, be), be, "after reload");

        // What clients get to draw it carries the same covers
        CoverableBlockEntity client = new CoverableBlockEntity(be.getBlockPos(), be.getBlockState());
        client.loadWithComponents(be.getUpdateTag(registries(helper)), registries(helper));
        check(helper, client.getCoverData().equals(be.getCoverData()), "update tag: cover data");
        helper.succeed();
    }

    @CBGameTest
    public void slopeKeepsCoversAndKind(GameTestHelper helper) {
        BlockState state = SlopeContent.CARPENTERS_SLOPE.defaultBlockState();
        CoverableBlockEntity be = place(helper, state, CoverableBlockEntity.class);
        decorate(be);
        checkSame(helper, reload(helper, be), be, "slope");
        // The kind is the block state's, so it travels in the chunk; the picked item carries it
        ItemStack picked = SlopeContent.CARPENTERS_SLOPE.getCloneItemStack(helper.getLevel(), be.getBlockPos(), state);
        check(helper, SlopeItem.kind(picked) == SlopeKind.of(state.getValue(CarpentersSlopeBlock.SLOPE)),
                "picked kind");
        helper.succeed();
    }

    @CBGameTest
    public void flowerPotKeepsItsContents(GameTestHelper helper) {
        FlowerPotBlockEntity pot = place(helper, FlowerPotContent.CARPENTERS_FLOWER_POT.defaultBlockState(),
                FlowerPotBlockEntity.class);
        pot.setCover(CoverData.BASE, Blocks.BRICKS.defaultBlockState());
        pot.setSoil(new ItemStack(Items.DIRT));
        pot.setPlant(new ItemStack(Items.POPPY));
        pot.setFertilizer(new ItemStack(Items.BONE_MEAL));
        pot.setAngle(11);
        FlowerPotBlockEntity loaded = reload(helper, pot);
        checkSame(helper, loaded, pot, "pot");
        check(helper, ItemStack.matches(loaded.getSoil(), pot.getSoil()), "soil");
        check(helper, ItemStack.matches(loaded.getPlant(), pot.getPlant()), "plant");
        check(helper, ItemStack.matches(loaded.getFertilizer(), pot.getFertilizer()), "fertilizer");
        check(helper, loaded.getAngle() == 11, "angle " + loaded.getAngle());
        helper.succeed();
    }

    @CBGameTest
    public void bedKeepsItsDesign(GameTestHelper helper) {
        CoverableBlockEntity bed = place(helper, BedContent.CARPENTERS_BED.defaultBlockState(),
                CoverableBlockEntity.class);
        bed.setCover(CoverData.BASE, Blocks.SPRUCE_PLANKS.defaultBlockState());
        bed.setDesign("stripes");
        CoverableBlockEntity loaded = reload(helper, bed);
        checkSame(helper, loaded, bed, "bed");
        check(helper, loaded.getDesign().equals("stripes"), "design " + loaded.getDesign());
        helper.succeed();
    }

    @CBGameTest
    public void safeKeepsItemsUpgradeAndAutomation(GameTestHelper helper) {
        SafeBlockEntity safe = place(helper, SafeContent.CARPENTERS_SAFE.defaultBlockState(), SafeBlockEntity.class);
        safe.setCover(CoverData.BASE, Blocks.IRON_BLOCK.defaultBlockState());
        safe.setOwner(OWNER);
        safe.setUpgrade(new ItemStack(Items.DIAMOND));
        safe.setItem(0, new ItemStack(Items.APPLE, 5));
        safe.setItem(SafeBlockEntity.UPGRADED_SLOTS - 1, new ItemStack(Items.EMERALD, 64));
        safe.setAutomation(SafeAutomation.ALL);
        SafeBlockEntity loaded = reload(helper, safe);
        checkSame(helper, loaded, safe, "safe");
        check(helper, ItemStack.matches(loaded.getUpgrade(), safe.getUpgrade()), "upgrade");
        check(helper, loaded.getContainerSize() == SafeBlockEntity.UPGRADED_SLOTS, "upgraded size");
        check(helper, ItemStack.matches(loaded.getItem(0), new ItemStack(Items.APPLE, 5)), "slot 0");
        check(helper, ItemStack.matches(loaded.getItem(SafeBlockEntity.UPGRADED_SLOTS - 1),
                new ItemStack(Items.EMERALD, 64)), "slot 53");
        check(helper, loaded.getAutomation() == SafeAutomation.ALL, "automation " + loaded.getAutomation());
        check(helper, loaded.getCapacity() == safe.getCapacity(), "capacity lights");
        helper.succeed();
    }

    @CBGameTest
    public void collapsibleKeepsItsDepths(GameTestHelper helper) {
        CoverableBlockEntity be = place(helper, CollapsibleContent.CARPENTERS_COLLAPSIBLE_BLOCK.defaultBlockState(),
                CoverableBlockEntity.class);
        Depths depths = new Depths(16, 3, 0, 9);
        CarpentersCollapsibleBlock.setDepths(be, depths);
        be.setCover(CoverData.BASE, Blocks.GRASS_BLOCK.defaultBlockState());
        CoverableBlockEntity loaded = reload(helper, be);
        checkSame(helper, loaded, be, "collapsible");
        check(helper, Depths.of(loaded.getBlockData()).equals(depths), "depths");
        helper.succeed();
    }

    @CBGameTest
    public void tileKeepsDesignDyeAndTurn(GameTestHelper helper) {
        helper.setBlock(POS, Blocks.STONE);
        CarpentersTileEntity tile = new CarpentersTileEntity(helper.getLevel(), helper.absolutePos(POS.north()),
                Direction.NORTH);
        tile.setDesign("stock_grid");
        tile.setDye(DyeColor.LIME);
        tile.setRotation(3);
        CompoundTag tag = tile.saveWithoutId(new CompoundTag());
        CarpentersTileEntity loaded = new CarpentersTileEntity(helper.getLevel(), BlockPos.ZERO, Direction.UP);
        loaded.load(tag);
        check(helper, loaded.getDesign().equals("stock_grid"), "design " + loaded.getDesign());
        check(helper, loaded.getDye() == DyeColor.LIME, "dye " + loaded.getDye());
        check(helper, loaded.getRotation() == 3, "rotation " + loaded.getRotation());
        check(helper, loaded.getDirection() == Direction.NORTH, "facing " + loaded.getDirection());
        check(helper, loaded.getPos().equals(tile.getPos()), "position");
        helper.succeed();
    }

    @CBGameTest
    public void setblockWithDataLoadsIt(GameTestHelper helper) {
        helper.setBlock(POS.below(), Blocks.STONE);
        BlockPos abs = helper.absolutePos(POS);
        String command = String.format("setblock %d %d %d carpentersblocks:carpenters_block[shape=full]"
                + "{Covers:[{Slot:6b,State:{Name:\"minecraft:stone\"},Dye:\"blue\",ChiselDesign:\"brick\"}]}",
                abs.getX(), abs.getY(), abs.getZ());
        helper.getLevel().getServer().getCommands().performPrefixedCommand(
                helper.getLevel().getServer().createCommandSourceStack().withSuppressedOutput(), command);
        CoverableBlockEntity be = (CoverableBlockEntity) helper.getBlockEntity(POS);
        CoverData data = be.getCoverData();
        check(helper, Blocks.STONE.defaultBlockState().equals(data.cover(CoverData.BASE)), "cover");
        check(helper, data.dye(CoverData.BASE) == DyeColor.BLUE, "dye");
        check(helper, "brick".equals(data.design(CoverData.BASE)), "design");
        // And clients would draw the same
        CoverableBlockEntity client = new CoverableBlockEntity(be.getBlockPos(), be.getBlockState());
        client.loadWithComponents(be.getUpdateTag(registries(helper)), registries(helper));
        check(helper, client.getCoverData().equals(data), "update tag");
        helper.succeed();
    }

    // Ctrl pick-block

    /** As Ctrl pick-block: the block entity's data saved onto the item, then the item placed on the floor. */
    private static BlockEntity pickAndPlace(GameTestHelper helper, BlockEntity be, ItemStack stack, BlockPos at) {
        be.saveToItem(stack, registries(helper));
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos floor = helper.absolutePos(at.below());
        stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor).add(0, 0.5, 0), Direction.UP, floor, false)));
        BlockEntity placed = helper.getBlockEntity(at);
        if (placed == null) {
            helper.fail("nothing placed at " + at);
        }
        return placed;
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 3; ++x) {
            for (int z = 0; z < 3; ++z) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    @CBGameTest
    public void pickBlockKeepsCovers(GameTestHelper helper) {
        floor(helper);
        CoverableBlockEntity be = place(helper, ModContent.CARPENTERS_BLOCK.defaultBlockState(),
                CoverableBlockEntity.class);
        be.setCover(CoverData.BASE, Blocks.BRICKS.defaultBlockState());
        be.setDye(CoverData.BASE, DyeColor.CYAN, new ItemStack(Items.CYAN_DYE));
        Block block = ModContent.CARPENTERS_BLOCK;
        CoverableBlockEntity copy = (CoverableBlockEntity) pickAndPlace(helper, be,
                block.getCloneItemStack(helper.getLevel(), be.getBlockPos(), be.getBlockState()), POS.east());
        check(helper, copy.getCoverData().equals(be.getCoverData()), "covers and dye copied");
        helper.succeed();
    }

    @CBGameTest
    public void pickBlockKeepsSlopeCoversAndKind(GameTestHelper helper) {
        floor(helper);
        CoverableBlockEntity be = place(helper, SlopeContent.CARPENTERS_SLOPE.defaultBlockState(),
                CoverableBlockEntity.class);
        be.setCover(CoverData.BASE, Blocks.SANDSTONE.defaultBlockState());
        ItemStack picked = SlopeContent.CARPENTERS_SLOPE.getCloneItemStack(helper.getLevel(), be.getBlockPos(),
                be.getBlockState());
        SlopeKind kind = SlopeItem.kind(picked);
        CoverableBlockEntity copy = (CoverableBlockEntity) pickAndPlace(helper, be, picked, POS.east());
        check(helper, copy.getCoverData().equals(be.getCoverData()), "covers copied");
        check(helper, SlopeKind.of(copy.getBlockState().getValue(CarpentersSlopeBlock.SLOPE)) == kind,
                "kind copied: " + copy.getBlockState().getValue(CarpentersSlopeBlock.SLOPE));
        helper.succeed();
    }

    /** Ctrl pick-block is creative-only and copies contents, as a vanilla chest's does. */
    @CBGameTest
    public void pickBlockCopiesSafeContentsAsVanilla(GameTestHelper helper) {
        floor(helper);
        SafeBlockEntity safe = place(helper, SafeContent.CARPENTERS_SAFE.defaultBlockState(), SafeBlockEntity.class);
        safe.setCover(CoverData.BASE, Blocks.IRON_BLOCK.defaultBlockState());
        safe.setItem(0, new ItemStack(Items.APPLE, 5));
        SafeBlockEntity copy = (SafeBlockEntity) pickAndPlace(helper, safe,
                SafeContent.CARPENTERS_SAFE.getCloneItemStack(helper.getLevel(), safe.getBlockPos(),
                        safe.getBlockState()), POS.east());
        check(helper, copy.getCoverData().equals(safe.getCoverData()), "covers copied");
        check(helper, ItemStack.matches(copy.getItem(0), new ItemStack(Items.APPLE, 5)), "contents copied");
        check(helper, ItemStack.matches(safe.getItem(0), new ItemStack(Items.APPLE, 5)), "original untouched");
        helper.succeed();
    }
}
