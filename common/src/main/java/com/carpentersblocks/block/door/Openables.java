package com.carpentersblocks.block.door;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
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

    /** Tells the player how the door now opens, as the original's activation messages. */
    public static void rigidityMessage(Player player, boolean rigid) {
        player.displayClientMessage(Component.translatable(
                "message." + CarpentersBlocks.MOD_ID + (rigid ? ".activation_iron" : ".activation_wood")), false);
    }
}
