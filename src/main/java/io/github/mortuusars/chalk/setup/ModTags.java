package io.github.mortuusars.chalk.setup;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;

public class ModTags {
    public static final class Items {
        public static final Tag.Named<Item> CHALK = ItemTags.bind("forge:chalk");
    }
}
