package io.github.mortuusars.chalk.setup;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static final class Items {
        public static final TagKey<Item> CHALK = ItemTags.create(new ResourceLocation("chalk:chalk"));
        public static final TagKey<Item> GLOWING = ItemTags.create(new ResourceLocation("chalk:glowing"));
    }

    public static final class Blocks {
        public static final TagKey<Block> CHALK_MARK = BlockTags.create(new ResourceLocation("chalk:chalk_mark"));
    }
}
