package io.github.mortuusars.chalk.data;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.setup.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(DataGenerator generator) { super(generator); }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> recipeBuilder) {
        ModItems.CHALKS.forEach( (name, item) -> {

            DyeColor color = DyeColor.byName(name.replace("chalk:", "").replace("_chalk", ""), DyeColor.WHITE);

            ShapelessRecipeBuilder.shapeless(item.get(), 1)
                    .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                    .group("chalk:chalk")
                    .requires(Items.CLAY_BALL)
                    .requires(ForgeRegistries.ITEMS.getValue(new ResourceLocation(color + "_dye")).asItem())
                    .save(recipeBuilder, Chalk.MOD_ID + ":chalk_from_" + color + "_dye");
        });
    }
}
