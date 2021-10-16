package io.github.mortuusars.chalk.setup;

import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;

public class ModTags {
    public static final class Items {
        public static final ITag.INamedTag<Item> CHALK = ItemTags.bind("forge:chalk");
    }
}
