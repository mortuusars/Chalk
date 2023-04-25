package io.github.mortuusars.chalk.data;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public enum Lang {
    CHALK_BOX_DRAWING_WITH_TOOLTIP("item", "chalk_box.tooltip.drawing_with"),
    CHALK_BOX_OPEN_TOOLTIP("item", "chalk_box.tooltip.open")

    ;

    public final String key;

    Lang(String category, String key) {
        this.key = category + "." + Chalk.ID + "." + key;
    }

    Lang(Item item) {
        this(item.getDescriptionId());
    }

    Lang(Block block) {
        this(block.getDescriptionId());
    }

    Lang(String key) {
        this.key = key;
    }

    public MutableComponent translate(Object... args) {
        return Component.translatable(key, args);
    }
}
