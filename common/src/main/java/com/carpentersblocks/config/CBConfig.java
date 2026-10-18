package com.carpentersblocks.config;

/**
 * All configuration values, with the original mod's defaults (ports of {@code FeatureRegistry}, {@code BlockRegistry}
 * and {@code ItemRegistry}). Each loader fills these in from its own config file at startup; game code only reads
 * them.
 */
public final class CBConfig {

    // Features
    public static boolean enableCovers = true;
    public static boolean enableOverlays = true;
    public static boolean enableSideCovers = true;
    public static boolean enableDyeColors = true;
    public static boolean enableChiselDesigns = true;
    public static boolean enableTorchWeatherEffects = true;
    public static boolean enableOwnership = true;
    public static boolean enableIllumination = true;
    public static boolean enableRoutableFluids = false;
    public static boolean enableAlphaPanes = true;
    public static boolean enableRailSlopes = true;
    public static boolean enableGarageDoorVoidFill = true;
    public static boolean enableFreeStandingLadders = false;
    public static int slopeSmoothness = 2;
    public static int multiBlockSizeLimit = 500;

    // Blocks
    public static boolean enableBarrier = true;
    public static boolean enableBed = true;
    public static boolean enableBlock = true;
    public static boolean enableButton = true;
    public static boolean enableCollapsibleBlock = true;
    public static boolean enableDaylightSensor = true;
    public static boolean enableDoor = true;
    public static boolean enableFlowerPot = true;
    public static boolean enableGarageDoor = true;
    public static boolean enableGate = true;
    public static boolean enableHatch = true;
    public static boolean enableLadder = true;
    public static boolean enableLever = true;
    public static boolean enablePressurePlate = true;
    public static boolean enableSafe = true;
    public static boolean enableSlope = true;
    public static boolean enableStairs = true;
    public static boolean enableTorch = true;

    public static int recipeQuantityBarrier = 4;
    public static int recipeQuantityBed = 1;
    public static int recipeQuantityBlock = 5;
    public static int recipeQuantityButton = 1;
    public static int recipeQuantityCollapsibleBlock = 9;
    public static int recipeQuantityDaylightSensor = 1;
    public static int recipeQuantityDoor = 1;
    public static int recipeQuantityFlowerPot = 1;
    public static int recipeQuantityGarageDoor = 8;
    public static int recipeQuantityGate = 1;
    public static int recipeQuantityHatch = 1;
    public static int recipeQuantityLadder = 4;
    public static int recipeQuantityLever = 1;
    public static int recipeQuantityPressurePlate = 1;
    public static int recipeQuantitySafe = 1;
    public static int recipeQuantitySlope = 6;
    public static int recipeQuantityStairs = 4;
    public static int recipeQuantityTorch = 8;

    // Items
    public static boolean enableHammer = true;
    public static boolean enableChisel = true;
    public static boolean enableTile = true;
    public static int toolUses = 400;
    public static boolean toolsDamageable = true;
    public static double itemHammerDamageChanceFromSlopes = 0.75;
    public static double itemHammerDamageChanceFromStairs = 1.0;
    public static double itemHammerDamageChanceFromCollapsible = 0.2;
    public static int recipeQuantityTile = 12;

    private CBConfig() {}
}
