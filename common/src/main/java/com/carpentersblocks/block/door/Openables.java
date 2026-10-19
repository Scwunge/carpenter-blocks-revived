package com.carpentersblocks.block.door;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.Nullable;

import com.carpentersblocks.CarpentersBlocks;

/** What the door, hatch and garage door share: rigidity (opening only by redstone) and the door sound. */
public final class Openables {

    /** The original's rigidity: a rigid door behaves like an iron door and only opens by redstone. */
    public static final BooleanProperty RIGID = BooleanProperty.create("rigid");

    private Openables() {}

    /**
     * The original's sound 1003, a wooden door opening or closing, with the vibration a vanilla door sends.
     */
    public static void playToggle(Level level, BlockPos pos, @Nullable Player player, boolean open) {
        level.playSound(null, pos, open ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE,
                SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
    }

    /** Pieces each explosion has already toggled, so a blast reaching several pieces of one door toggles it once. */
    private static final Map<Explosion, Set<BlockPos>> TOGGLED = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Whether a wind charge's blast should toggle the door made of {@code pieces}: it can trigger blocks and hasn't
     * already reached another piece of the same door, as vanilla toggles a door only from its lower half.
     */
    public static boolean windToggles(Explosion explosion, Collection<BlockPos> pieces) {
        if (!explosion.canTriggerBlocks()) {
            return false;
        }
        Set<BlockPos> done = TOGGLED.computeIfAbsent(explosion, e -> new HashSet<>());
        boolean first = Collections.disjoint(done, pieces);
        done.addAll(pieces);
        return first;
    }

    /** Tells the player how the door now opens, as the original's activation messages. */
    public static void rigidityMessage(Player player, boolean rigid) {
        player.displayClientMessage(Component.translatable(
                "message." + CarpentersBlocks.MOD_ID + (rigid ? ".activation_iron" : ".activation_wood")), false);
    }
}
