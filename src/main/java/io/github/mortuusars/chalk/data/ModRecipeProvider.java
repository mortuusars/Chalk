package io.github.mortuusars.chalk.data;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.setup.ModItems;
import io.github.mortuusars.chalk.setup.ModTags;
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

//            DyeColor color = DyeColor.byName(name.replace("chalk:", "").replace("_chalk", ""), DyeColor.WHITE);
            DyeColor color = item.get().getColor();

            ShapelessRecipeBuilder.shapeless(item.get(), 1)
                    .unlockedBy("has_calcite", has(Items.CALCITE))
                    .group("chalk:chalk")
                    .requires(Items.CALCITE)
                    .requires(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(color + "_dye"))).asItem())
                    .save(recipeBuilder, Chalk.MOD_ID + ":chalk_from_" + color + "_dye");
        });

        ModItems.GLOWING_CHALKS.forEach( (name, item) -> {
            DyeColor color = item.get().getColor();
            ShapelessRecipeBuilder.shapeless(item.get(), 1)
                    .unlockedBy("has_chalk", has(ModTags.Items.CHALK))
                    .unlockedBy("has_glowing_ink", has(Items.GLOW_INK_SAC))
                    .group("chalk:glowing_chalk")
                    .requires(Items.GLOW_INK_SAC)
                    .requires(ModItems.getChalkByColor(color))
                    .save(recipeBuilder, Chalk.MOD_ID + ":normal_to_glowing_" + color + "_chalk");
        });
    }
}
