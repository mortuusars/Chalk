package io.github.mortuusars.chalk.data.generation;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(DataGenerator generator, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, blockTagsProvider, Chalk.ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(Chalk.Tags.Items.GLOWINGS)
                .add(Items.GLOW_INK_SAC)
                .add(Items.GLOWSTONE_DUST);

        Chalk.Items.CHALKS.forEach((color, item) -> {
            tag(Chalk.Tags.Items.CHALKS).add(item.get());
            tag(Chalk.Tags.Items.FORGE_CHALKS).add(item.get());
        });

        tag(Chalk.Tags.Items.ALLOWED_IN_CHALK_BOX)
                .addTag(Chalk.Tags.Items.CHALKS);

        for (DyeColor color : DyeColor.values()){
            tag(color.getTag()).add(Chalk.Items.getChalk(color));
        }
    }
}
