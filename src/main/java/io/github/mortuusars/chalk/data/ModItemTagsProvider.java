package io.github.mortuusars.chalk.data;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.setup.ModItems;
import io.github.mortuusars.chalk.setup.ModTags;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.DyeColor;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(DataGenerator generator, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, blockTagsProvider, Chalk.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {

        ModItems.CHALKS.forEach((name, item) -> {
            tag(ModTags.Items.CHALK).add(item.get());
        });

        for (DyeColor color : DyeColor.values()){
            tag(color.getTag()).add(ModItems.getChalkByColor(color));
        }
    }
}
