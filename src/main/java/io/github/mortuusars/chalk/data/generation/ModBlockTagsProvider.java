package io.github.mortuusars.chalk.data.generation;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), provider, Chalk.ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
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
