package io.github.mortuusars.chalk.data;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, Chalk.ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        Chalk.Blocks.MARKS.forEach((color, block) -> tag(Chalk.Tags.Blocks.CHALK_MARK).add(block.get()));
    }
}
