package com.carpentersblocks.block.coverable;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DeadBushBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.api.ICarpentersChisel;
import com.carpentersblocks.api.ICarpentersHammer;
import com.carpentersblocks.config.CBConfig;
import com.carpentersblocks.platform.Platform;

/**
 * Base of every Carpenter's block: something that can be covered, dyed, overlaid, chiseled and lit, and that takes
 * on the properties of its cover. Port of the original {@code BlockCoverable}.
 * <p>
 * Right- and left-clicks reach {@link #use} and {@link #attack} through each loader's click events rather than
 * vanilla's block callbacks, because vanilla skips the block when the player sneaks with an item in hand (which the
 * original relies on for dyes, overlays and removing attributes) and doesn't tell the block which face was hit.
 * Both run on the client too, to predict the outcome; only the server changes anything.
 */
public abstract class CoverableBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    /** Light given off, from the covers or the illuminator. Kept in the state because vanilla caches light by state. */
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);
    /** Modern addition: frames that aren't full blocks can hold water. */
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected CoverableBlock(Properties properties) {
        super(properties);
    }

    /** The properties shared by the original's wooden blocks, with light driven by {@link #LIGHT}. */
    public static BlockBehaviour.Properties baseProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .instrument(NoteBlockInstrument.BASS)
                .strength(1.5F)
                .sound(SoundType.WOOD)
                .ignitedByLava()
                .lightLevel(state -> state.hasProperty(LIGHT) ? state.getValue(LIGHT) : 0);
    }

    /** Subclasses adding properties must call this and set every property in their default state. */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT, WATERLOGGED);
    }

    // Waterlogging (modern addition)

    /** Whether a block placed by this context starts waterlogged. */
    protected static boolean placedInWater(BlockPlaceContext context) {
        return context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoverableBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    public static CoverableBlockEntity blockEntity(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CoverableBlockEntity be ? be : null;
    }

    @Nullable
    protected static BlockState baseCover(BlockGetter level, BlockPos pos) {
        CoverableBlockEntity be = blockEntity(level, pos);
        return be == null ? null : be.getCoverData().cover(CoverData.BASE);
    }

    // Hooks for subclasses, matching the original's

    /** Whether a cover may go in the slot (0-5 sides, 6 base). By default only the base takes a cover. */
    protected boolean canCoverSide(CoverableBlockEntity be, int slot) {
        return slot == CoverData.BASE;
    }

    private boolean canCover(CoverableBlockEntity be, int slot) {
        return (slot == CoverData.BASE || CBConfig.enableSideCovers) && canCoverSide(be, slot);
    }

    /** Hammer left-click that isn't removing an attribute. Return true if the block changed. */
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        return false;
    }

    /** Hammer right-click. Return true if the block changed. */
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        return false;
    }

    /** Runs before attributes are tried on a left-click with the hammer; mark the result altered to stop there. */
    protected void preOnBlockClicked(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face, ActionResult result) {}

    /** Runs before attributes are tried on a right-click; mark the result altered to stop there. */
    protected void preOnBlockActivated(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, InteractionHand hand, BlockHitResult hit, ActionResult result) {}

    /** Runs when a right-click applied no attribute (opening doors, pressing buttons and so on). */
    protected void postOnBlockActivated(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, InteractionHand hand, BlockHitResult hit, ActionResult result) {}

    /** Whether the player may activate the block at all (the safe uses this). */
    protected boolean canPlayerActivate(CoverableBlockEntity be, Player player) {
        return true;
    }

    /** Lets a block redirect activation to another block entity, such as the other half of a door. */
    protected CoverableBlockEntity getBlockEntityForActivation(CoverableBlockEntity be) {
        return be;
    }

    /**
     * The block state matching the attributes, called whenever they change. Sets {@link #LIGHT}; blocks that store
     * more (such as solidity) extend this.
     */
    public BlockState stateForCovers(BlockState state, CoverData data) {
        int light = 0;
        if (CBConfig.enableIllumination && data.isIlluminated()) {
            light = 15;
        } else {
            for (int slot = 0; slot < CoverData.SLOTS; ++slot) {
                BlockState cover = data.cover(slot);
                if (cover != null) {
                    light = Math.max(light, cover.getLightEmission());
                }
            }
        }
        return state.hasProperty(LIGHT) ? state.setValue(LIGHT, light) : state;
    }

    // Right-click

    /**
     * Handles a right-click on the block, as the original {@code onBlockActivated}: the block's own handling first,
     * then tools, covers, and (while sneaking) illuminators, overlays and dyes, then the block's fallback.
     *
     * @return the click result, or {@link InteractionResult#PASS} to let vanilla carry on (placing blocks etc.)
     */
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        CoverableBlockEntity be = blockEntity(level, pos);
        if (be == null || !canPlayerActivate(be, player)) {
            return InteractionResult.PASS;
        }
        be = getBlockEntityForActivation(be);
        boolean server = !level.isClientSide;
        ActionResult result = new ActionResult();

        preOnBlockActivated(state, level, pos, be, player, hand, hit, result);

        if (!result.altered && Permissions.hasElevatedPermission(be, player, false)) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.isEmpty()) {
                applyHeldItem(state, level, pos, be, player, hand, hit, stack, server, result);
            }
        }

        if (!result.altered) {
            postOnBlockActivated(state, level, pos, be, player, hand, hit, result);
        } else if (server) {
            if (result.soundSource == null) {
                BlockState base = be.getCoverData().cover(CoverData.BASE);
                result.soundSource = base == null ? state : base;
            }
            damageTool(level, player, hand);
        }

        if (server && result.altered && result.playSound && result.soundSource != null) {
            playPlaceSound(level, be.getBlockPos(), result.soundSource);
        }
        if (server && result.decInventory) {
            player.getItemInHand(hand).consume(1, player);
        }
        return result.altered ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    private void applyHeldItem(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be, Player player,
            InteractionHand hand, BlockHitResult hit, ItemStack stack, boolean server, ActionResult result) {
        CoverData data = be.getCoverData();
        Direction face = hit.getDirection();
        int faceSlot = face.get3DDataValue();
        int slot = data.hasCover(faceSlot) ? faceSlot : CoverData.BASE;

        if (stack.getItem() instanceof ICarpentersHammer hammer && hammer.canUseHammer(level, player)) {
            if (onHammerRightClick(state, level, pos, be, player, face)) {
                result.setAltered();
            }
            return;
        }

        if (stack.getItem() instanceof ICarpentersChisel chisel) {
            if (CBConfig.enableChisel && chisel.canUseChisel(level, player) && data.hasCover(slot)
                    && onChiselClick(level, be, slot, false, server)) {
                result.setAltered();
            }
            return;
        }

        BlockState cover = CBConfig.enableCovers ? AttributeItems.coverStateFor(stack, player, hand, hit) : null;
        if (cover != null) {
            // The base takes the first cover; once it has one, covers go on the clicked side
            if (slot == CoverData.BASE && (!canCover(be, CoverData.BASE) || data.hasCover(CoverData.BASE))) {
                slot = faceSlot;
            }
            if (canCover(be, slot) && !data.hasCover(slot)) {
                if (server) {
                    be.setCover(slot, cover);
                }
                result.setAltered().decInventory().setSoundSource(cover);
            }
            return;
        }

        if (!player.isSecondaryUseActive()) {
            return;
        }

        Overlay overlay;
        DyeColor dye;
        if (CBConfig.enableIllumination && AttributeItems.isIlluminator(stack)) {
            if (!data.isIlluminated()) {
                if (server) {
                    be.setIlluminator(stack);
                }
                result.setAltered().decInventory().setSoundSource(soundSourceFor(stack));
            }
        } else if (CBConfig.enableOverlays && (overlay = Overlay.forItem(stack)) != null) {
            if (data.overlay(slot) == null && (slot == CoverData.BASE || data.hasCover(slot))) {
                if (server) {
                    be.setOverlay(slot, overlay, stack);
                }
                result.setAltered().decInventory().setSoundSource(soundSourceFor(stack));
            }
        } else if (CBConfig.enableDyeColors && (dye = AttributeItems.dyeFor(stack)) != null) {
            if (data.dye(slot) == null) {
                if (server) {
                    be.setDye(slot, dye, stack);
                }
                result.setAltered().decInventory().setSoundSource(soundSourceFor(stack));
            }
        }
    }

    /** The original played a block item's own sound, or sand for anything else. */
    private static BlockState soundSourceFor(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem ? blockItem.getBlock().defaultBlockState()
                : Blocks.SAND.defaultBlockState();
    }

    private static void playPlaceSound(Level level, BlockPos pos, BlockState source) {
        SoundType sound = source.getSoundType();
        level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F,
                sound.getPitch() * 0.8F);
    }

    private static void damageTool(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof ICarpentersHammer hammer) {
            hammer.onHammerUse(level, player, hand);
        } else if (stack.getItem() instanceof ICarpentersChisel chisel) {
            chisel.onChiselUse(level, player, hand);
        }
    }

    // Left-click

    /**
     * Handles a left-click, as the original {@code onBlockClicked}: with the hammer, sneaking removes the outermost
     * attribute on the clicked face and otherwise the block reacts; with the chisel, sneaking removes the design and
     * otherwise cycles back through designs.
     *
     * @return true if the click was taken by a Carpenter's tool, so the block must not start breaking
     */
    public boolean attack(BlockState state, Level level, BlockPos pos, Player player, Direction face) {
        ItemStack stack = player.getMainHandItem();
        boolean hammer = stack.getItem() instanceof ICarpentersHammer;
        boolean chisel = stack.getItem() instanceof ICarpentersChisel;
        if (!hammer && !chisel) {
            return false;
        }
        CoverableBlockEntity be = blockEntity(level, pos);
        if (be == null || level.isClientSide || !Permissions.hasElevatedPermission(be, player, false)) {
            return be != null;
        }

        CoverData data = be.getCoverData();
        int faceSlot = face.get3DDataValue();
        int slot = data.hasCover(faceSlot) ? faceSlot : CoverData.BASE;

        if (hammer && ((ICarpentersHammer) stack.getItem()).canUseHammer(level, player)) {
            ActionResult result = new ActionResult();
            preOnBlockClicked(state, level, pos, be, player, face, result);
            if (!result.altered) {
                if (player.isSecondaryUseActive()) {
                    ItemStack drop = be.popAttribute(slot);
                    if (!drop.isEmpty()) {
                        Block.popResourceFromFace(level, pos, face, drop);
                    }
                } else {
                    onHammerLeftClick(state, level, pos, be, player, face);
                }
            } else {
                level.updateNeighborsAt(pos, this);
            }
        } else if (chisel && ((ICarpentersChisel) stack.getItem()).canUseChisel(level, player)) {
            if (player.isSecondaryUseActive() && data.design(slot) != null) {
                be.setChiselDesign(slot, null);
            } else if (data.hasCover(slot)) {
                onChiselClick(level, be, slot, true, true);
            }
        }
        return true;
    }

    /**
     * Sets the chisel design of a slot: an unset slot copies a design from a neighboring Carpenter's block if one
     * has a design on the same slot, otherwise designs cycle forwards (right-click) or backwards (left-click).
     */
    protected boolean onChiselClick(Level level, CoverableBlockEntity be, int slot, boolean leftClick,
            boolean apply) {
        if (!CBConfig.enableChiselDesigns) {
            return false;
        }
        String design = be.getCoverData().design(slot);
        String adjacent = null;
        if (design == null) {
            for (Direction dir : Direction.values()) {
                CoverableBlockEntity neighbor = blockEntity(level, be.getBlockPos().relative(dir));
                if (neighbor != null && neighbor.getCoverData().design(slot) != null) {
                    adjacent = neighbor.getCoverData().design(slot);
                }
            }
        }
        String next = adjacent != null ? adjacent : leftClick ? ChiselDesigns.prev(design) : ChiselDesigns.next(design);
        if (apply) {
            be.setChiselDesign(slot, next);
        }
        return true;
    }

    // Properties that follow the cover

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        // Carpenter's tools never break blocks
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof ICarpentersHammer || held.getItem() instanceof ICarpentersChisel) {
            return 0.0F;
        }
        BlockState cover = baseCover(level, pos);
        if (cover != null && !(cover.getBlock() instanceof CoverableBlock)) {
            return cover.getDestroyProgress(player, level, pos);
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    /** Blast resistance of the base cover. Overrides NeoForge's block extension; Fabric calls it from a mixin. */
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        BlockState cover = baseCover(level, pos);
        if (cover != null && !(cover.getBlock() instanceof CoverableBlock)) {
            return cover.getBlock().getExplosionResistance();
        }
        return getExplosionResistance();
    }

    /**
     * What a face looks like it is made of: a full-face overlay if there is one, otherwise the face's cover, or
     * {@code null} for the bare frame. Port of {@code BlockProperties.getFeatureSensitiveSideItemStack}; a
     * {@code null} face means the base.
     */
    @Nullable
    public static BlockState featureSensitiveState(BlockGetter level, BlockPos pos, @Nullable Direction face) {
        CoverableBlockEntity be = blockEntity(level, pos);
        if (be == null) {
            return null;
        }
        CoverData data = be.getCoverData();
        if (face == null) {
            return data.cover(CoverData.BASE);
        }
        int slot = data.slotFor(face);
        Overlay overlay = data.overlay(slot);
        if (overlay != null && overlay.coversFullSide(face)) {
            return overlay.block().defaultBlockState();
        }
        return data.cover(slot);
    }

    /**
     * Sound of the block. Footsteps (which come with an entity) sound like the top face, as the original's step
     * sounds did; digging and breaking sound like the base cover. Overrides NeoForge's block extension; Fabric
     * reaches it through mixins.
     */
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState source = featureSensitiveState(level, pos, entity != null ? Direction.UP : null);
        return source != null && !(source.getBlock() instanceof CoverableBlock) ? source.getSoundType()
                : state.getSoundType();
    }

    /** Slipperiness of the top face (ice covers are slippery). Overrides NeoForge's block extension. */
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        BlockState top = topCover(level, pos);
        return top != null ? top.getBlock().getFriction() : getFriction();
    }

    // Fire, following the face's cover. The bare frame burns like the original (encouragement 5, flammability 20).

    public static final int FRAME_FLAMMABILITY = 20;
    public static final int FRAME_FIRE_SPREAD = 5;

    /** Overrides NeoForge's block extension; Fabric reaches it through a mixin. */
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        BlockState source = featureSensitiveState(level, pos, face);
        return source == null || source.getBlock() instanceof CoverableBlock ? FRAME_FLAMMABILITY
                : Platform.get().flammability(source, level, pos, face);
    }

    /** Overrides NeoForge's block extension; Fabric reaches it through a mixin. */
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        BlockState source = featureSensitiveState(level, pos, face);
        return source == null || source.getBlock() instanceof CoverableBlock ? FRAME_FIRE_SPREAD
                : Platform.get().fireSpreadSpeed(source, level, pos, face);
    }

    /** Overrides NeoForge's block extension. */
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return getFlammability(state, level, pos, face) > 0;
    }

    /** Overrides NeoForge's block extension: netherrack covers burn forever. */
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction face) {
        BlockState source = featureSensitiveState(level, pos, face);
        return source != null && !(source.getBlock() instanceof CoverableBlock)
                && Platform.get().isFireSource(source, level, pos, face);
    }

    // Plants, as the original's canSustainPlant

    /** Kinds of plant soil, matching the original's plant types. */
    public enum PlantType { PLAINS, DESERT, NETHER, BEACH }

    /**
     * Whether a plant may grow on the given face, or {@code null} to leave it to the plant. The face must be solid,
     * and the top cover, base cover and their overlays decide which plants take: grass and dirt for flowers and
     * saplings, sand for cactus and dead bushes, soul sand for nether wart, and grass, dirt or sand next to water
     * for sugar cane.
     */
    @Nullable
    public Boolean sustainsPlant(BlockState state, BlockGetter level, BlockPos pos, Direction face,
            @Nullable PlantType type) {
        CoverableBlockEntity be = blockEntity(level, pos);
        if (be == null || type == null) {
            return null;
        }
        if (!state.isFaceSturdy(level, pos, face)) {
            return false;
        }

        CoverData data = be.getCoverData();
        boolean grassOrDirt = false;
        boolean sand = false;
        boolean soulSand = false;
        for (int slot : new int[] { Direction.UP.get3DDataValue(), CoverData.BASE }) {
            Overlay overlay = data.overlay(slot);
            for (BlockState soil : new BlockState[] { data.cover(slot),
                    overlay == null ? null : overlay.block().defaultBlockState() }) {
                if (soil != null) {
                    grassOrDirt |= soil.is(BlockTags.DIRT);
                    sand |= soil.is(BlockTags.SAND);
                    soulSand |= soil.is(Blocks.SOUL_SAND) || soil.is(Blocks.SOUL_SOIL);
                }
            }
        }

        return switch (type) {
            case PLAINS -> grassOrDirt;
            case DESERT -> sand;
            case NETHER -> soulSand;
            case BEACH -> (grassOrDirt || sand) && hasAdjacentWater(level, pos);
        };
    }

    private static boolean hasAdjacentWater(BlockGetter level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(pos.relative(dir)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    /** The original's plant type for a plant block, or {@code null} for plants it left alone. */
    @Nullable
    public static PlantType plantTypeOf(BlockState plant) {
        Block block = plant.getBlock();
        if (block instanceof CactusBlock || block instanceof DeadBushBlock) {
            return PlantType.DESERT;
        }
        if (block instanceof NetherWartBlock) {
            return PlantType.NETHER;
        }
        if (block instanceof SugarCaneBlock) {
            return PlantType.BEACH;
        }
        if (block instanceof BushBlock && !(block instanceof CropBlock) && !(block instanceof WaterlilyBlock)
                && !(block instanceof MushroomBlock)) {
            return PlantType.PLAINS;
        }
        return null;
    }

    // Behavior borrowed from covers

    /** Side covers come off when the side is pressed flat against a solid neighbor, as in the original. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        CoverableBlockEntity be = blockEntity(level, pos);
        if (level.isClientSide || be == null) {
            return;
        }
        for (Direction dir : Direction.values()) {
            int slot = dir.get3DDataValue();
            if (!be.getCoverData().hasCover(slot)) {
                continue;
            }
            BlockPos adjacent = pos.relative(dir);
            if (!canCover(be, slot) || level.getBlockState(adjacent).isFaceSturdy(level, adjacent, dir.getOpposite())
                    && state.isFaceSturdy(level, pos, dir)) {
                ejectSlot(level, pos, be, slot);
            }
        }
    }

    /** Effects of the base cover (dripping leaves, and so on) and of a mycelium overlay. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        CoverableBlockEntity be = blockEntity(level, pos);
        if (be == null) {
            return;
        }
        BlockState base = be.getCoverData().cover(CoverData.BASE);
        try {
            if (base != null) {
                base.getBlock().animateTick(base, level, pos, random);
            }
            if (be.getCoverData().overlay(CoverData.BASE) == Overlay.MYCELIUM) {
                Blocks.MYCELIUM.animateTick(Blocks.MYCELIUM.defaultBlockState(), level, pos, random);
            }
        } catch (RuntimeException ignored) {
            // A cover's effect expected its own block in the world; skip it
        }
    }

    /** Standing on the top cover: magma burns. A modern addition; 1.7 had none of these blocks. */
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        BlockState top = topCover(level, pos);
        if (top != null) {
            top.getBlock().stepOn(level, pos, top, entity);
        } else {
            super.stepOn(level, pos, state, entity);
        }
    }

    /** Landing on the top cover: hay and slime soften falls. A modern addition. */
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        BlockState top = topCover(level, pos);
        if (top != null) {
            top.getBlock().fallOn(level, top, pos, entity, fallDistance);
        } else {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
    }

    /** Slime covers bounce. A modern addition. */
    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        BlockState top = topCover(level, entity.getOnPos());
        if (top != null) {
            top.getBlock().updateEntityAfterFallOn(level, entity);
        } else {
            super.updateEntityAfterFallOn(level, entity);
        }
    }

    @Nullable
    private static BlockState topCover(BlockGetter level, BlockPos pos) {
        BlockState top = featureSensitiveState(level, pos, Direction.UP);
        return top == null || top.getBlock() instanceof CoverableBlock ? null : top;
    }

    /** Weak power: the strongest signal any cover gives. */
    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        CoverableBlockEntity be = blockEntity(level, pos);
        int power = 0;
        if (be != null) {
            for (int slot = 0; slot < CoverData.SLOTS; ++slot) {
                BlockState cover = be.getCoverData().cover(slot);
                if (cover != null) {
                    power = Math.max(power, cover.getSignal(level, pos, direction));
                }
            }
        }
        return power;
    }

    /** Strong power: the cover on the side facing the receiver, or the base cover. */
    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        CoverableBlockEntity be = blockEntity(level, pos);
        if (be == null) {
            return 0;
        }
        BlockState cover = be.getCoverData().coverFor(direction.getOpposite());
        return cover == null ? 0 : cover.getSignal(level, pos, direction);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CoverableBlockEntity be = blockEntity(level, pos);
        if (!level.isClientSide && be != null && placer instanceof Player player) {
            be.setOwner(player.getUUID());
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            CoverableBlockEntity be = blockEntity(level, pos);
            if (be != null) {
                for (ItemStack drop : be.getAttributeDrops()) {
                    popResource(level, pos, drop);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** Drops the attributes of a slot whose cover can no longer stay, as the original's neighbor check does. */
    protected void ejectSlot(Level level, BlockPos pos, CoverableBlockEntity be, int slot) {
        List<ItemStack> drops = new ArrayList<>(be.removeSlot(slot));
        for (ItemStack drop : drops) {
            popResource(level, pos, drop);
        }
    }

    /** Helper for item use damage, shared by the tools. */
    public static void damageItem(Player player, InteractionHand hand) {
        if (CBConfig.toolsDamageable && !player.getAbilities().instabuild) {
            player.getItemInHand(hand).hurtAndBreak(1, player,
                    hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
    }

    /**
     * What a click did, so sounds, item use and neighbor updates happen once at the end. Port of the original's
     * {@code ActionResult}.
     */
    public static final class ActionResult {

        @Nullable
        public BlockState soundSource;
        public boolean playSound = true;
        public boolean altered;
        public boolean decInventory;

        public ActionResult setSoundSource(BlockState source) {
            soundSource = source;
            return this;
        }

        public ActionResult setNoSound() {
            playSound = false;
            return this;
        }

        public ActionResult setAltered() {
            altered = true;
            return this;
        }

        public ActionResult decInventory() {
            decInventory = true;
            return this;
        }
    }
}
