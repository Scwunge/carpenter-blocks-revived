package com.carpentersblocks.block.redstone;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;

/** What presses a Carpenter's Pressure Plate, in the original's order. */
public enum PlateTrigger implements StringRepresentable {
    PLAYER("player"),
    MONSTER("monster"),
    ANIMAL("animal"),
    ALL("all");

    private final String name;

    PlateTrigger(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public PlateTrigger next() {
        return values()[(ordinal() + 1) % values().length];
    }

    /** By the entity's spawn category, which modded mobs declare too, as the original's creature types. */
    public boolean triggeredBy(Entity entity) {
        return switch (this) {
            case PLAYER -> entity instanceof Player;
            case MONSTER -> entity.getType().getCategory() == MobCategory.MONSTER;
            case ANIMAL -> entity.getType().getCategory() == MobCategory.CREATURE;
            case ALL -> true;
        };
    }
}
