package io.github.mortuusars.chalk.data.generation;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), lookupProvider, blockTagsProvider.contentsGetter(), Chalk.ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(Chalk.Tags.Items.GLOWINGS)
                .add(Items.GLOW_INK_SAC)
                .add(Items.GLOWSTONE_DUST);

        Chalk.Items.CHALKS.forEach((color, item) -> {
            tag(Chalk.Tags.Items.CHALKS).add(item.get());
            tag(Chalk.Tags.Items.FORGE_CHALKS).add(item.get());
        });

        for (DyeColor color : DyeColor.values()){
            tag(color.getTag()).add(Chalk.Items.getChalk(color));
        }
    }
}
