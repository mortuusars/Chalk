package io.github.mortuusars.chalk.data;


import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.setup.ModItems;
import net.minecraft.data.DataGenerator;
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

        singleTexture("chalk_box", mcLoc("item/generated"), "layer0", modLoc("item/chalk_box"));
    }
}
