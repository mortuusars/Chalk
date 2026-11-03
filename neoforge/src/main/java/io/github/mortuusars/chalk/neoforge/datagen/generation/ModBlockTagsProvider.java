package io.github.mortuusars.chalk.neoforge.datagen.generation;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, Chalk.ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(Chalk.Tags.Blocks.CHALK_CANNOT_DRAW_ON)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.SAPLINGS)
                .addTag(BlockTags.FLOWERS)
                .add(BlockItemIds.SLIME_BLOCK.block())
                .add(BlockItemIds.HONEYCOMB_BLOCK.block())
                .add(BlockItemIds.SCAFFOLDING.block())
                .add(BlockItemIds.SCULK.block())
                .add(BlockItemIds.SCULK_CATALYST.block())
                .add(BlockItemIds.SCULK_SHRIEKER.block())
                .add(BlockItemIds.SCULK_SENSOR.block())
        ;
    }
}
