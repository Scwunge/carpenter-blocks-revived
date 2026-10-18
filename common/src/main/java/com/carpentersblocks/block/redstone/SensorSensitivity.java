package com.carpentersblocks.block.redstone;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

/** How a Carpenter's Daylight Sensor reads the sky, in the original's order. */
public enum SensorSensitivity implements StringRepresentable {
    /** Full power above the light players can sleep in: on from sunrise to sunset. */
    SLEEP("sleep"),
    /** Full power above the light monsters spawn in, storms counting as dark. */
    MONSTERS("monsters"),
    /** Power follows the sun's height as seen from the face the sensor is on. */
    DYNAMIC("dynamic");

    private final String name;

    SensorSensitivity(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public SensorSensitivity next() {
        return values()[(ordinal() + 1) % values().length];
    }

    /** Output before polarity for a reading of 0 to 15, as the original's {@code getRedstoneOutput}. */
    public int output(int reading) {
        return switch (this) {
            case SLEEP -> reading > 11 ? 15 : 0;
            case MONSTERS -> reading > 7 ? 15 : 0;
            case DYNAMIC -> reading;
        };
    }

    /**
     * The reading the sensor stores, as the original's {@code updateLightLevel}.
     *
     * @param skylight  sky light at the sensor less the sky's darkening
     * @param sunAngle  the sun's angle in radians, 0 at noon
     */
    public int reading(Direction facing, int skylight, float sunAngle, boolean thundering) {
        int light = skylight;
        if (this == DYNAMIC) {
            if (sunAngle <= 1.67F || sunAngle >= 4.62F) {
                light = switch (facing) {
                    case UP -> celestial(light, sunAngle);
                    case NORTH, SOUTH -> (int) (light * 0.6F);
                    case EAST -> celestial(light, (float) (sunAngle + Math.PI / 2));
                    case WEST -> celestial(light, (float) (sunAngle - Math.PI / 2));
                    case DOWN -> light;
                };
            } else {
                light = 0;
            }
        } else if (this == MONSTERS && thundering) {
            light = 7;
        }
        return Mth.clamp(light, 0, 15);
    }

    /** The original's {@code getCelestialRedstoneOutput}: sky light scaled by the sun's height, pulled towards noon. */
    static int celestial(int skylight, float angle) {
        if (angle < (float) Math.PI) {
            angle += (0.0F - angle) * 0.2F;
        } else {
            angle += ((float) Math.PI * 2F - angle) * 0.2F;
        }
        return Mth.clamp(Math.round(skylight * Mth.cos(angle)), 0, 15);
    }
}
