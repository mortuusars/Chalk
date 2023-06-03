package io.github.mortuusars.chalk.render;

import net.minecraft.world.item.DyeColor;

import java.util.HashMap;

public class ChalkColors {
    public static final HashMap<DyeColor, Integer> COLORS = new HashMap<>(){{
        put(DyeColor.BLACK, 0x252525);
        put(DyeColor.RED, 0xeb4a39);
        put(DyeColor.GREEN, 0x51a80b);
        put(DyeColor.BROWN, 0x8a522a);
        put(DyeColor.BLUE, 0x3b50d2);
        put(DyeColor.PURPLE, 0xa74cd2);
        put(DyeColor.CYAN, 0x1dcac0);
        put(DyeColor.LIGHT_GRAY, 0xadada8);
        put(DyeColor.GRAY, 0x606466);
        put(DyeColor.PINK, 0xee658e);
        put(DyeColor.LIME, 0x9ae437);
        put(DyeColor.YELLOW, 0xffd929);
        put(DyeColor.LIGHT_BLUE, 0x82dbf8);
        put(DyeColor.MAGENTA, 0xed60e2);
        put(DyeColor.ORANGE, 0xff8034);
        put(DyeColor.WHITE, 0xffffff);
    }};

    public static int fromDyeColor(DyeColor color){
        return COLORS.get(color);
    }
}
