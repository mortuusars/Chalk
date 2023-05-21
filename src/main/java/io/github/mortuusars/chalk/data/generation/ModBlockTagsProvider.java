package io.github.mortuusars.chalk.data.generation;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, Chalk.ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        Chalk.Blocks.MARKS.forEach((color, block) -> tag(Chalk.Tags.Blocks.CHALK_MARKS).add(block.get()));

        tag(Chalk.Tags.Blocks.CHALK_CANNOT_DRAW_ON)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.SAPLINGS)
                .addTag(BlockTags.FLOWERS)
                .add(Blocks.SLIME_BLOCK)
                .add(Blocks.HONEYCOMB_BLOCK)
                .add(Blocks.SCAFFOLDING)
                .add(Blocks.SCULK)
                .add(Blocks.SCULK_CATALYST)
                .add(Blocks.SCULK_SHRIEKER)
                .add(Blocks.SCULK_SENSOR)
        ;
    }
}
