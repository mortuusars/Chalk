package io.github.mortuusars.chalk.data;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public enum Lang {
    CHALK_BOX_DRAWING_WITH_TOOLTIP("item", "chalk_box.tooltip.drawing_with"),
    CHALK_BOX_OPEN_TOOLTIP("item", "chalk_box.tooltip.open"),

    SYMBOL_HOUSE("gui", "symbol.house"),
    SYMBOL_CHECK("gui", "symbol.check"),
    SYMBOL_CROSS("gui", "symbol.cross"),
    SYMBOL_HEART("gui", "symbol.heart"),
    SYMBOL_SKULL("gui", "symbol.skull"),

    MESSAGE_NO_SYMBOLS_UNLOCKED("gui", "no_symbols_unlocked")
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
