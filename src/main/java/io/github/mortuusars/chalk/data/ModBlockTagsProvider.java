package io.github.mortuusars.chalk.data;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.setup.ModBlocks;
import io.github.mortuusars.chalk.setup.ModTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, Chalk.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        ModBlocks.MARKS.forEach( (name, block) -> tag(ModTags.Blocks.CHALK_MARK).add(block.get()));
    }
}
