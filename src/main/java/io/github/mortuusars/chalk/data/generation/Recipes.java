package io.github.mortuusars.chalk.data.generation;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes(DataGenerator generator) {
        super(generator.getPackOutput());
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> recipeConsumer) {
        Chalk.Items.CHALKS.forEach((color, item) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, item.get(), 1)
                .unlockedBy("has_calcite", has(Items.CALCITE))
                .group("chalk:chalk")
                .requires(Items.CALCITE)
                .requires(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(color + "_dye"))).asItem())
                .save(recipeConsumer, Chalk.ID + ":chalk_from_" + color + "_dye"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Chalk.Items.CHALK_BOX.get())
                .unlockedBy("has_chalk", has(Chalk.Tags.Items.CHALKS))
                .unlockedBy("has_paper", has(Items.PAPER))
                .unlockedBy("has_slimeball", has(Tags.Items.SLIMEBALLS))
                .pattern("P P")
                .pattern("PSP")
                .pattern("PPP")
                .define('P', Items.PAPER)
                .define('S', Tags.Items.SLIMEBALLS)
                .save(recipeConsumer);
    }
}
