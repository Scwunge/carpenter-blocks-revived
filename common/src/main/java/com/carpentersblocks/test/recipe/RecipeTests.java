package com.carpentersblocks.test.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.config.CBConfig;
import com.carpentersblocks.recipe.ConfiguredShapedRecipe;
import com.carpentersblocks.registry.TileContent;
import com.carpentersblocks.test.CBGameTest;

/** Recipe quantities: configured shaped recipes take their count from the config option when loaded. */
public class RecipeTests {

    private static final String TILE_RECIPE = """
            {"type": "carpentersblocks:configured_shaped", "category": "misc", "pattern": ["XXX", "YYY"],
             "key": {"X": {"item": "minecraft:terracotta"}, "Y": {"item": "carpentersblocks:carpenters_block"}},
             "result": {"id": "carpentersblocks:carpenters_tile", "count": 12},
             "quantity_option": "recipeQuantityTile"}""";

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    @CBGameTest
    public void loadedRecipesGiveTheDefaultCount(GameTestHelper helper) {
        RecipeHolder<?> holder = helper.getLevel().getRecipeManager()
                .byKey(CarpentersBlocks.id("carpenters_tile")).orElse(null);
        check(helper, holder != null && holder.value() instanceof ShapedRecipe, "the tile recipe is loaded, shaped");
        check(helper, holder.value().getSerializer() == RecipeSerializer.SHAPED_RECIPE, "syncs as vanilla shaped");
        int count = holder.value().getResultItem(helper.getLevel().registryAccess()).getCount();
        check(helper, count == CBConfig.recipeQuantityTile, "default count: " + count);
        helper.succeed();
    }

    @CBGameTest
    public void countFollowsTheOption(GameTestHelper helper) {
        int saved = CBConfig.recipeQuantityTile;
        try {
            CBConfig.recipeQuantityTile = 3;
            JsonObject json = JsonParser.parseString(TILE_RECIPE).getAsJsonObject();
            ConfiguredShapedRecipe recipe = ConfiguredShapedRecipe.CODEC.codec()
                    .parse(helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE), json)
                    .getOrThrow();
            check(helper, recipe.getResultItem(helper.getLevel().registryAccess()).is(TileContent.CARPENTERS_TILE_ITEM)
                    && recipe.getResultItem(helper.getLevel().registryAccess()).getCount() == 3, "3 tiles");
            json.addProperty("quantity_option", "enableTile");
            check(helper, ConfiguredShapedRecipe.CODEC.codec().parse(
                    helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE), json).isError(),
                    "a true/false option is refused");
        } finally {
            CBConfig.recipeQuantityTile = saved;
        }
        helper.succeed();
    }
}
