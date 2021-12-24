package io.github.mortuusars.chalk.data;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.setup.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(DataGenerator generator) { super(generator); }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> recipeBuilder) {
        ModItems.CHALKS.forEach( (name, item) -> {

            DyeColor color = DyeColor.byName(name.replace("chalk:", "").replace("_chalk", ""), DyeColor.WHITE);

            ShapelessRecipeBuilder.shapeless(item.get(), 1)
                    .unlockedBy("has_calcite", has(Items.CALCITE))
                    .group("chalk:chalk")
                    .requires(Items.CALCITE)
                    .requires(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(color + "_dye"))).asItem())
                    .save(recipeBuilder, Chalk.MOD_ID + ":chalk_from_" + color + "_dye");
        });
    }
}
