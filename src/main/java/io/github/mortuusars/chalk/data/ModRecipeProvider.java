package io.github.mortuusars.chalk.data;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.setup.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Items;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(DataGenerator generator) { super(generator); }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> recipeBuilder) {
        ShapelessRecipeBuilder.shapeless(ModItems.WHITE_CHALK.get(), 1)
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .group("chalk:chalk")
                .requires(Items.CLAY_BALL)
                .requires(Items.WHITE_DYE)
                .save(recipeBuilder, Chalk.MOD_ID + ":chalk_from_white_dye");

        ShapelessRecipeBuilder.shapeless(ModItems.RED_CHALK.get(), 1)
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .group("chalk:chalk")
                .requires(Items.CLAY_BALL)
                .requires(Items.RED_DYE)
                .save(recipeBuilder, Chalk.MOD_ID + ":chalk_from_red_dye");
    }
}
