package com.carpentersblocks.block.flowerpot;

import java.util.function.BiPredicate;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.block.coverable.AttributeItems;
import com.carpentersblocks.block.coverable.CoverData;
import com.carpentersblocks.block.coverable.CoverableBlock;
import com.carpentersblocks.block.coverable.CoverableBlockEntity;
import com.carpentersblocks.block.coverable.Overlay;
import com.carpentersblocks.block.coverable.Permissions;
import com.carpentersblocks.block.design.DesignList;
import com.carpentersblocks.config.CBConfig;

/**
 * Carpenter's Flower Pot: a small pot that takes a cover (or a glaze design in its place), soil, a plant and
 * fertilizer. Port of the original {@code BlockCarpentersFlowerPot}.
 * <p>
 * Soil goes in by clicking the soil area on top (or anywhere once the pot is covered, since soil blocks are covers
 * too); a plant goes in once there is soil. Sneak left-click with the hammer takes out the fertilizer, then the plant
 * (clicking high), or the soil (clicking the soil area). The hammer otherwise cycles the glaze, dropping the cover.
 * Bone meal on a plant that takes a tint (grass, ferns, leaves) turns it a richer green.
 */
public class CarpentersFlowerPotBlock extends CoverableBlock {

    public static final MapCodec<CarpentersFlowerPotBlock> CODEC = simpleCodec(CarpentersFlowerPotBlock::new);

    public static final EnumProperty<PotContents> CONTENTS = EnumProperty.create("contents", PotContents.class);

    private static final VoxelShape POT_SHAPE = PotGeometry.EMPTY_OUTLINE.shape();
    private static final VoxelShape PLANT_SHAPE = PotGeometry.PLANT_OUTLINE.shape();
    private static final VoxelShape SOLID_SHAPE = PotGeometry.SOLID_OUTLINE.shape();

    /**
     * Client side: if the plant in the pot at a position takes a tint, which only the client knows (block colors are
     * client-side), asks the server to fertilize it with an {@code EnrichPlantPayload} and returns true. Set by the
     * client; the server takes the client's word, as the original's enrich packet did.
     */
    public static BiPredicate<Level, BlockPos> requestEnrich = (level, pos) -> false;

    public CarpentersFlowerPotBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(CONTENTS, PotContents.EMPTY)
                .setValue(LIGHT, 0)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONTENTS);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FlowerPotBlockEntity(pos, state);
    }

    @Nullable
    private static FlowerPotBlockEntity pot(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FlowerPotBlockEntity pot ? pot : null;
    }

    // Placement: on anything a torch stands on, as the original

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(WATERLOGGED, placedInWater(context));
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    /** A tall plant comes out when something solid is put on top of it, as the original. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        FlowerPotBlockEntity pot = pot(level, pos);
        if (level.isClientSide || pot == null || !pot.hasPlant()) {
            return;
        }
        BlockState plant = PotPlants.plantState(pot.getPlant());
        BlockPos above = pos.above();
        if (plant != null && PotPlants.profile(plant) == PotPlants.Profile.DOUBLE
                && level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN)) {
            popResource(level, pos, pot.getPlant().copy());
            pot.setPlant(ItemStack.EMPTY);
        }
    }

    // Shapes: the outline grows with the plant; only the pot (or a solid plant) collides, as the original

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(CONTENTS)) {
            case EMPTY -> POT_SHAPE;
            case PLANT -> PLANT_SHAPE;
            case SOLID -> SOLID_SHAPE;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return state.getValue(CONTENTS) == PotContents.SOLID ? SOLID_SHAPE : POT_SHAPE;
    }

    // Covers and designs

    /** Only the base takes a cover, and not while a design is shown in its place. */
    @Override
    protected boolean canCoverSide(CoverableBlockEntity be, int slot) {
        return slot == CoverData.BASE && be.getDesign().isEmpty();
    }

    @Override
    protected boolean onHammerLeftClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        setDesign(level, pos, be, DesignList.FLOWER_POT.previous(be.getDesign()));
        return true;
    }

    @Override
    protected boolean onHammerRightClick(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face) {
        if (!level.isClientSide) {
            setDesign(level, pos, be, player.isSecondaryUseActive() ? "" : DesignList.FLOWER_POT.next(be.getDesign()));
        }
        return true;
    }

    /** A design replaces the cover, which drops, as the original. */
    private void setDesign(Level level, BlockPos pos, CoverableBlockEntity be, String design) {
        if (!design.isEmpty() && be.getCoverData().hasCover(CoverData.BASE)) {
            ejectSlot(level, pos, be, CoverData.BASE);
        }
        be.setDesign(design);
    }

    // Soil, plant and fertilizer

    /** Whether a click at {@code hit} lands on the soil area of the top face. */
    private static boolean onSoilArea(BlockPos pos, Direction face, Vec3 hit) {
        return face == Direction.UP && PotGeometry.onSoil(hit.x - pos.getX(), hit.z - pos.getZ());
    }

    @Override
    protected void preOnBlockActivated(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, InteractionHand hand, BlockHitResult hit, ActionResult result) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !(be instanceof FlowerPotBlockEntity pot)
                || !Permissions.hasElevatedPermission(be, player, false)) {
            return;
        }
        boolean server = !level.isClientSide;

        if (stack.is(PotPlants.FERTILIZERS)) {
            // Only the client can tell whether the plant takes a tint, so it asks the server to fertilize
            if (!server && pot.hasPlant() && !pot.isFertilized() && requestEnrich.test(level, pos)) {
                result.setAltered().setNoSound();
            }
            return;
        }

        CoverData data = be.getCoverData();
        boolean hasCover = data.hasCover(CoverData.BASE);
        boolean soilArea = onSoilArea(pos, hit.getDirection(), hit.getLocation());

        if (pot.hasSoil()) {
            // Leaves can be a plant or a cover: off the soil area they cover the pot first
            if (!soilArea && (!hasCover && canCoverSide(be, CoverData.BASE) && CBConfig.enableCovers
                    && AttributeItems.coverStateFor(stack, player, hand, hit) != null
                    || data.overlay(CoverData.BASE) == null && CBConfig.enableOverlays
                            && player.isSecondaryUseActive() && Overlay.forItem(stack) != null)) {
                return;
            }
            BlockState plant = PotPlants.plantState(stack);
            if (!pot.hasPlant() && plant != null) {
                if (server) {
                    pot.setAngle(PotGeometry.angle(player.getYRot()));
                    pot.setPlant(stack);
                }
                result.setAltered().decInventory().setSoundSource(plant);
            }
        } else if (PotPlants.isSoil(stack) && (hasCover || soilArea)) {
            if (server) {
                pot.setSoil(stack);
            }
            result.setAltered().decInventory().setSoundSource(PotPlants.soilState(stack));
        }
    }

    /** Sneak left-click with the hammer: the fertilizer, then the plant up high; the soil on the soil area. */
    @Override
    protected void preOnBlockClicked(BlockState state, Level level, BlockPos pos, CoverableBlockEntity be,
            Player player, Direction face, ActionResult result) {
        if (!player.isSecondaryUseActive() || !(be instanceof FlowerPotBlockEntity pot)) {
            return;
        }
        HitResult pick = player.pick(player.blockInteractionRange(), 1.0F, false);
        if (!(pick instanceof BlockHitResult hit) || !hit.getBlockPos().equals(pos)) {
            return;
        }
        Vec3 location = hit.getLocation();
        ItemStack drop = ItemStack.EMPTY;
        if (location.y - pos.getY() > PotGeometry.PLANT_CLICK_HEIGHT) {
            if (pot.isFertilized()) {
                drop = pot.getFertilizer().copy();
                pot.setFertilizer(ItemStack.EMPTY);
            } else if (pot.hasPlant()) {
                drop = pot.getPlant().copy();
                pot.setPlant(ItemStack.EMPTY);
            }
        } else if (pot.hasSoil() && !pot.hasPlant() && onSoilArea(pos, hit.getDirection(), location)) {
            drop = pot.getSoil().copy();
            pot.setSoil(ItemStack.EMPTY);
        }
        if (!drop.isEmpty()) {
            popResourceFromFace(level, pos, face, drop);
            result.setAltered();
        }
    }

    /** Server side: fertilizes the plant, for {@code EnrichPlantPayload}. */
    public static void enrich(Level level, BlockPos pos, Player player) {
        FlowerPotBlockEntity pot = pot(level, pos);
        if (pot == null || !pot.hasPlant() || pot.isFertilized()
                || !Permissions.hasElevatedPermission(pot, player, false)) {
            return;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(PotPlants.FERTILIZERS)) {
                pot.setFertilizer(stack);
                stack.consume(1, player);
                level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 15);
                return;
            }
        }
    }

    // Behavior borrowed from the plant and soil

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        FlowerPotBlockEntity pot = pot(level, pos);
        BlockState plant = pot == null ? null : PotPlants.plantState(pot.getPlant());
        if (plant != null) {
            try {
                plant.entityInside(level, pos, entity);
            } catch (RuntimeException ignored) {
                // The plant expected its own block in the world
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        FlowerPotBlockEntity pot = pot(level, pos);
        if (pot == null) {
            return;
        }
        for (BlockState inside : new BlockState[] { PotPlants.plantState(pot.getPlant()),
                PotPlants.soilState(pot.getSoil()) }) {
            if (inside != null) {
                try {
                    inside.getBlock().animateTick(inside, level, pos, random);
                } catch (RuntimeException ignored) {
                    // As above
                }
            }
        }
    }
}
