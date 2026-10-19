package com.carpentersblocks.entity;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.api.ICarpentersHammer;
import com.carpentersblocks.block.coverable.AttributeItems;
import com.carpentersblocks.block.coverable.Permissions;
import com.carpentersblocks.block.design.DesignList;
import com.carpentersblocks.registry.TileContent;

/**
 * Carpenter's Tile: a one-pixel-thick tile on any face of a block, with a design, a dye color and a turn. Port of the
 * original {@code EntityCarpentersTile}, built on vanilla's hanging entities (like item frames) so it stays on its
 * block, drops when the block goes and is knocked off when pushed.
 * <p>
 * With the hammer, left-click shows the next design and right-click the previous one; sneak right-click turns the
 * design and sneak left-click takes the tile down. A dye applied while sneaking colors it. In creative, left-click
 * without the hammer removes it.
 */
public class CarpentersTileEntity extends HangingEntity {

    private static final EntityDataAccessor<String> DATA_DESIGN = SynchedEntityData.defineId(CarpentersTileEntity.class,
            EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_DYE = SynchedEntityData.defineId(CarpentersTileEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ROTATION = SynchedEntityData.defineId(
            CarpentersTileEntity.class, EntityDataSerializers.INT);

    private static final String TAG_FACING = "Facing";
    private static final String TAG_DESIGN = "Design";
    private static final String TAG_DYE = "Dye";
    private static final String TAG_ROTATION = "Rotation";
    private static final String TAG_OWNER = "Owner";

    /** Neighbors closer than this (in blocks, beyond the tile's own cell) pass on their look, as the original. */
    private static final double MATCH_REACH = 0.2;

    private static final int SURVIVAL_CHECK_TICKS = 20;

    @Nullable
    private UUID owner;

    public CarpentersTileEntity(EntityType<? extends CarpentersTileEntity> type, Level level) {
        super(type, level);
    }

    /** A tile in the cell {@code pos}, on the face of the block behind it that points {@code direction}. */
    public CarpentersTileEntity(Level level, BlockPos pos, Direction direction) {
        super(TileContent.CARPENTERS_TILE, level, pos);
        setDirection(direction);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_DESIGN, "");
        builder.define(DATA_DYE, DyeColor.WHITE.getId());
        builder.define(DATA_ROTATION, 0);
    }

    // Look

    /** The design's name, or {@code ""} for the blank tile. */
    public String getDesign() {
        String design = entityData.get(DATA_DESIGN);
        return DesignList.TILE.isValid(design) ? design : "";
    }

    public void setDesign(String design) {
        entityData.set(DATA_DESIGN, design);
    }

    public DyeColor getDye() {
        return DyeColor.byId(entityData.get(DATA_DYE));
    }

    public void setDye(DyeColor dye) {
        entityData.set(DATA_DYE, dye.getId());
    }

    /** Quarter turns of the design, 0 to 3. */
    public int getRotation() {
        return entityData.get(DATA_ROTATION);
    }

    public void setRotation(int rotation) {
        entityData.set(DATA_ROTATION, rotation & 3);
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    /**
     * Takes the look of a neighboring tile, as the original did when a tile was placed near the edge of a face (the
     * tile beyond that edge) or in its middle (the tiles along each edge). {@code edge} is {@code null} for the middle.
     */
    public void matchNeighbors(@Nullable Direction edge) {
        Direction.Axis facing = direction.getAxis();
        // Shrunk within the face so tiles that only touch this one's edges are left out
        double in = -0.1;
        AABB box = getBoundingBox().inflate(facing == Direction.Axis.X ? 0 : in, facing == Direction.Axis.Y ? 0 : in,
                facing == Direction.Axis.Z ? 0 : in);
        if (edge != null) {
            box = box.move(edge.getStepX() * MATCH_REACH, edge.getStepY() * MATCH_REACH,
                    edge.getStepZ() * MATCH_REACH);
        } else {
            double out = MATCH_REACH;
            box = box.inflate(facing == Direction.Axis.X ? 0 : out, facing == Direction.Axis.Y ? 0 : out,
                    facing == Direction.Axis.Z ? 0 : out);
        }
        for (CarpentersTileEntity tile : level().getEntitiesOfClass(CarpentersTileEntity.class, box,
                tile -> tile != this && tile.direction == direction)) {
            // From the middle, only tiles straight along an edge count, not diagonal ones
            if (edge == null && !alongAnEdge(tile.getPos())) {
                continue;
            }
            if (tile.getDye() != DyeColor.WHITE) {
                setDye(tile.getDye());
            }
            if (tile.getRotation() != 0) {
                setRotation(tile.getRotation());
            }
            if (!tile.getDesign().isEmpty()) {
                setDesign(tile.getDesign());
            }
        }
    }

    /** Whether {@code other} shares one of this tile's coordinates within the face, so it isn't diagonal. */
    private boolean alongAnEdge(BlockPos other) {
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (axis != direction.getAxis() && axis.choose(pos.getX(), pos.getY(), pos.getZ())
                    == axis.choose(other.getX(), other.getY(), other.getZ())) {
                return true;
            }
        }
        return false;
    }

    // Placement on a face

    @Override
    protected void setDirection(Direction direction) {
        this.direction = direction;
        if (direction.getAxis().isHorizontal()) {
            setXRot(0);
            setYRot(direction.get2DDataValue() * 90);
        } else {
            setXRot(-90 * direction.getAxisDirection().getStep());
            setYRot(0);
        }
        xRotO = getXRot();
        yRotO = getYRot();
        recalculateBoundingBox();
    }

    /** The cell squeezed against the block behind it, {@link TileGeometry#DEPTH} thick. */
    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction direction) {
        return TileGeometry.box(pos, direction);
    }

    /** Checks the face behind every second, as the original, rather than vanilla's every five. */
    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && !isRemoved() && tickCount % SURVIVAL_CHECK_TICKS == 0 && !survives()) {
            discard();
            dropItem(null);
        }
    }

    /** A tile stays while the face behind it is solid, as the original; other tiles and entities don't matter. */
    @Override
    public boolean survives() {
        BlockPos support = pos.relative(direction.getOpposite());
        return level().getBlockState(support).isFaceSturdy(level(), support, direction);
    }

    // Interaction

    private boolean mayAlter(Player player) {
        return Permissions.hasElevatedPermission(owner, player, false);
    }

    /** Hammer right-click: previous design, or a quarter turn when sneaking. Sneak + dye: color. */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean hammer = stack.getItem() instanceof ICarpentersHammer;
        DyeColor dye = hammer ? null : AttributeItems.anyDyeFor(stack);
        if (!hammer && (dye == null || !player.isSecondaryUseActive())) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide && mayAlter(player)) {
            if (hammer) {
                if (player.isSecondaryUseActive()) {
                    setRotation(getRotation() + 1);
                } else {
                    setDesign(DesignList.TILE.previous(getDesign()));
                }
                playSound(Blocks.TERRACOTTA.defaultBlockState());
            } else if (dye != getDye()) {
                setDye(dye);
                playSound(Blocks.SAND.defaultBlockState());
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    /**
     * Left-click. With the hammer: next design, or take the tile down when sneaking. In creative without the hammer:
     * remove it. Nothing else harms a tile.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isRemoved() || !(source.getEntity() instanceof Player player)
                || !mayAlter(player)) {
            return false;
        }
        boolean hammer = player.getMainHandItem().getItem() instanceof ICarpentersHammer;
        playSound(Blocks.TERRACOTTA.defaultBlockState());
        if (hammer && !player.isSecondaryUseActive()) {
            setDesign(DesignList.TILE.next(getDesign()));
            return false;
        }
        if (hammer || player.isCreative()) {
            kill();
            markHurt();
            dropItem(player);
            return true;
        }
        return false;
    }

    /** Drops the tile, unless a creative player without the hammer took it down. */
    @Override
    public void dropItem(@Nullable Entity breaker) {
        if (breaker instanceof Player player && player.isCreative()
                && !(player.getMainHandItem().getItem() instanceof ICarpentersHammer)) {
            return;
        }
        spawnAtLocation(new ItemStack(TileContent.CARPENTERS_TILE_ITEM));
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(TileContent.CARPENTERS_TILE_ITEM);
    }

    /** The original played terracotta (hardened clay) for the tile and sand for dyes. */
    private void playSound(BlockState source) {
        SoundType sound = source.getSoundType();
        level().playSound(null, blockPosition(), sound.getPlaceSound(), SoundSource.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
    }

    @Override
    public void playPlacementSound() {
        playSound(Blocks.TERRACOTTA.defaultBlockState());
    }

    // Saving and syncing

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte(TAG_FACING, (byte) direction.get3DDataValue());
        tag.putString(TAG_DESIGN, getDesign());
        tag.putString(TAG_DYE, getDye().getName());
        tag.putInt(TAG_ROTATION, getRotation());
        if (owner != null) {
            tag.putUUID(TAG_OWNER, owner);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (!tag.contains("TileX")) {
            // Summoned without a block position: the tile goes in the cell it was summoned in
            tag = tag.copy();
            tag.putInt("TileX", blockPosition().getX());
            tag.putInt("TileY", blockPosition().getY());
            tag.putInt("TileZ", blockPosition().getZ());
        }
        super.readAdditionalSaveData(tag);
        setDirection(Direction.from3DDataValue(tag.getByte(TAG_FACING)));
        setDesign(tag.getString(TAG_DESIGN));
        setDye(DyeColor.byName(tag.getString(TAG_DYE), DyeColor.WHITE));
        setRotation(tag.getInt(TAG_ROTATION));
        owner = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, direction.get3DDataValue(), getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        setDirection(Direction.from3DDataValue(packet.getData()));
    }
}
