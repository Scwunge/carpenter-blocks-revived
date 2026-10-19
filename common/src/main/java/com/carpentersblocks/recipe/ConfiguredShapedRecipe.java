package com.carpentersblocks.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import com.carpentersblocks.config.ConfigOptions;
import com.carpentersblocks.config.ConfigOptions.Option;

/**
 * A shaped recipe whose output count is a config option ({@code recipeQuantity*}), as the original's recipe quantity
 * settings. The count is read when the recipe is loaded, so {@code /reload} picks up a changed option.
 * <p>
 * Written like a vanilla shaped recipe with {@code "type": "carpentersblocks:configured_shaped"} and a
 * {@code "quantity_option"} naming the option. Once loaded it is an ordinary shaped recipe: it reports vanilla's
 * serializer, so clients and recipe viewers receive a plain shaped recipe with the configured count.
 */
public class ConfiguredShapedRecipe extends ShapedRecipe {

    private static final Codec<Option> QUANTITY_OPTION = Codec.STRING.comapFlatMap(ConfiguredShapedRecipe::option,
            Option::key);

    public static final MapCodec<ConfiguredShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                    .forGetter(ShapedRecipe::category),
            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.unconfiguredResult),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification),
            QUANTITY_OPTION.fieldOf("quantity_option").forGetter(recipe -> recipe.quantityOption))
            .apply(instance, ConfiguredShapedRecipe::new));

    public static final RecipeSerializer<ConfiguredShapedRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public MapCodec<ConfiguredShapedRecipe> codec() {
            return CODEC;
        }

        /** Never used: the recipe syncs as a vanilla shaped recipe (see {@link #getSerializer}). */
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ConfiguredShapedRecipe> streamCodec() {
            return StreamCodec.of((buf, recipe) -> {
                throw new UnsupportedOperationException("Configured shaped recipes sync as vanilla shaped recipes");
            }, buf -> {
                throw new UnsupportedOperationException("Configured shaped recipes sync as vanilla shaped recipes");
            });
        }
    };

    private final ShapedRecipePattern pattern;
    private final ItemStack unconfiguredResult;
    private final Option quantityOption;

    public ConfiguredShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
            ItemStack result, boolean showNotification, Option quantityOption) {
        super(group, category, pattern, result.copyWithCount((Integer) quantityOption.get()), showNotification);
        this.pattern = pattern;
        this.unconfiguredResult = result;
        this.quantityOption = quantityOption;
    }

    /** Vanilla's, so the recipe goes to clients as a plain shaped recipe. */
    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPED_RECIPE;
    }

    private static DataResult<Option> option(String key) {
        for (String section : ConfigOptions.SECTIONS) {
            for (Option option : ConfigOptions.inSection(section)) {
                if (option.key().equals(key)) {
                    return option.type() == Option.Type.INTEGER ? DataResult.success(option)
                            : DataResult.error(() -> "Config option " + key + " is not a whole number");
                }
            }
        }
        return DataResult.error(() -> "Unknown config option " + key);
    }
}
