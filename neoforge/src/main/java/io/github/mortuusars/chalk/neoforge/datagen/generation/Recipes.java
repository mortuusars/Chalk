package io.github.mortuusars.chalk.neoforge.datagen.generation;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.*;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.Tags;

public class Recipes extends RecipeProvider {
    public Recipes(BootstrapContext<Recipe<?>> recipeOutput, final BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    @Override
    protected void buildRecipes() {
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.TOOLS, Chalk.Items.CHALK.get(), 1)
              .unlockedBy("has_calcite", has(Items.CALCITE))
              .requires(Items.CALCITE)
              .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.TOOLS, Chalk.Items.CHALK_BOX.get())
                .unlockedBy("has_chalk", has(Chalk.Items.CHALK.get()))
                .unlockedBy("has_paper", has(Items.PAPER))
                .unlockedBy("has_slimeball", has(Tags.Items.SLIME_BALLS))
                .pattern("PSP")
                .pattern("PPP")
                .define('P', Items.PAPER)
                .define('S', Tags.Items.SLIME_BALLS)
                .save(output);
    }

    public void generate() {
        buildRecipes();
    }
}
