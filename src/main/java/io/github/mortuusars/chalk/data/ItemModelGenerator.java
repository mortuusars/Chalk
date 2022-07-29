package io.github.mortuusars.chalk.data;


import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.setup.ClientSetup;
import io.github.mortuusars.chalk.setup.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Chalk.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ModItems.CHALKS.forEach( (name, item) ->
            singleTexture(name, mcLoc("item/generated"), "layer0", modLoc("item/" + name)));

        ItemModelBuilder chalkBoxModelBuilder = getBuilder("chalk_box")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", "item/chalk_box");

        for (DyeColor color : DyeColor.values()) {
            int chalkId = color.getId() + 1;

            ItemModelBuilder chalkBoxWithChalkModel = getBuilder("chalk_box_" + color.getSerializedName())
                    .parent(getExistingFile(mcLoc("item/generated")))
                    .texture("layer0", "item/chalk_box")
                    .texture("layer1", "item/" + "chalk_box_" + color.getSerializedName() + "_chalk");

            chalkBoxModelBuilder.override().predicate(ClientSetup.CHALK_BOX_SELECTED_PROPERTY, chalkId).model(chalkBoxWithChalkModel).end();
        }
    }
}
