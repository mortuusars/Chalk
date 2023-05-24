package io.github.mortuusars.chalk.data.generation;


import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.items.ChalkBoxItem;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Chalk.ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        Chalk.Items.CHALKS.forEach((color, item) ->
            singleTexture(item.getId().getPath(), modLoc("item/chalk"), "layer0", modLoc("item/" + item.getId().getPath())));

        ItemModelBuilder chalkBoxModelBuilder = getBuilder("chalk_box")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", "item/chalk_box");

        for (DyeColor color : DyeColor.values()) {
            int colorID = color.getId() + 1;

            ItemModelBuilder chalkBoxWithChalkModel = getBuilder("chalk_box_" + color)
                    .parent(getExistingFile(mcLoc("item/generated")))
                    .texture("layer0", "item/chalk_box")
                    .texture("layer1", "item/" + "chalk_box_" + color + "_chalk");

            chalkBoxModelBuilder.override()
                    .predicate(ChalkBoxItem.SELECTED_PROPERTY, colorID)
                    .model(chalkBoxWithChalkModel)
                    .end();
        }
    }
}
