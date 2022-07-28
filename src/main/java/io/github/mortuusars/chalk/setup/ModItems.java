package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.items.ChalkBoxItem;
import io.github.mortuusars.chalk.items.ChalkItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;

public class ModItems {
    public static HashMap<String, RegistryObject<ChalkItem>> CHALKS = new HashMap<String, RegistryObject<ChalkItem>>();
    public static HashMap<String, RegistryObject<ChalkItem>> GLOWING_CHALKS = new HashMap<String, RegistryObject<ChalkItem>>();

    public static final RegistryObject<ChalkBoxItem> CHALK_BOX = Registry.ITEMS.register("chalk_box",
            () -> new ChalkBoxItem(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS).stacksTo(1)));

    public static RegistryObject<ChalkItem> BLACK_CHALK = registerColoredChalk(DyeColor.BLACK);
    public static RegistryObject<ChalkItem> RED_CHALK = registerColoredChalk(DyeColor.RED);
    public static RegistryObject<ChalkItem> GREEN_CHALK = registerColoredChalk(DyeColor.GREEN);
    public static RegistryObject<ChalkItem> BROWN_CHALK = registerColoredChalk(DyeColor.BROWN);
    public static RegistryObject<ChalkItem> BLUE_CHALK = registerColoredChalk(DyeColor.BLUE);
    public static RegistryObject<ChalkItem> PURPLE_CHALK = registerColoredChalk(DyeColor.PURPLE);
    public static RegistryObject<ChalkItem> CYAN_CHALK = registerColoredChalk(DyeColor.CYAN);
    public static RegistryObject<ChalkItem> LIGHT_GRAY_CHALK = registerColoredChalk(DyeColor.LIGHT_GRAY);
    public static RegistryObject<ChalkItem> GRAY_CHALK = registerColoredChalk(DyeColor.GRAY);
    public static RegistryObject<ChalkItem> PINK_CHALK = registerColoredChalk(DyeColor.PINK);
    public static RegistryObject<ChalkItem> LIME_CHALK = registerColoredChalk(DyeColor.LIME);
    public static RegistryObject<ChalkItem> YELLOW_CHALK = registerColoredChalk(DyeColor.YELLOW);
    public static RegistryObject<ChalkItem> LIGHT_BLUE_CHALK = registerColoredChalk(DyeColor.LIGHT_BLUE);
    public static RegistryObject<ChalkItem> MAGENTA_CHALK = registerColoredChalk(DyeColor.MAGENTA);
    public static RegistryObject<ChalkItem> ORANGE_CHALK = registerColoredChalk(DyeColor.ORANGE);
    public static RegistryObject<ChalkItem> WHITE_CHALK = registerColoredChalk(DyeColor.WHITE);

    public static RegistryObject<ChalkItem> BLACK_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.BLACK);
    public static RegistryObject<ChalkItem> RED_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.RED);
    public static RegistryObject<ChalkItem> GREEN_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.GREEN);
    public static RegistryObject<ChalkItem> BROWN_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.BROWN);
    public static RegistryObject<ChalkItem> BLUE_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.BLUE);
    public static RegistryObject<ChalkItem> PURPLE_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.PURPLE);
    public static RegistryObject<ChalkItem> CYAN_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.CYAN);
    public static RegistryObject<ChalkItem> LIGHT_GLOWING_GRAY_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.LIGHT_GRAY);
    public static RegistryObject<ChalkItem> GRAY_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.GRAY);
    public static RegistryObject<ChalkItem> PINK_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.PINK);
    public static RegistryObject<ChalkItem> LIME_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.LIME);
    public static RegistryObject<ChalkItem> YELLOW_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.YELLOW);
    public static RegistryObject<ChalkItem> LIGHT_GLOWING_BLUE_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.LIGHT_BLUE);
    public static RegistryObject<ChalkItem> MAGENTA_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.MAGENTA);
    public static RegistryObject<ChalkItem> ORANGE_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.ORANGE);
    public static RegistryObject<ChalkItem> WHITE_GLOWING_CHALK = registerGlowingColoredChalk(DyeColor.WHITE);

    public static void register(){}

    public static ChalkItem getChalkByColor(DyeColor color){
        return CHALKS.get(color.toString() + "_chalk").get();
    }

    public static ChalkItem getGlowingChalkByColor(DyeColor color){
        return GLOWING_CHALKS.get("glowing_" + color.toString() + "_chalk").get();
    }


    private static RegistryObject<ChalkItem> registerColoredChalk(DyeColor dyeColor){
        String registryName = dyeColor.toString() + "_chalk";
        RegistryObject<ChalkItem> item = Registry.ITEMS.register(registryName, () -> new ChalkItem(dyeColor, false, new Item.Properties()));
        CHALKS.put(registryName, item);
        return item;
    }

    private static RegistryObject<ChalkItem> registerGlowingColoredChalk(DyeColor dyeColor){
        String registryName = "glowing_" + dyeColor.toString() + "_chalk";
        RegistryObject<ChalkItem> item = Registry.ITEMS.register(registryName, () -> new ChalkItem(dyeColor, true, new Item.Properties()));
        GLOWING_CHALKS.put(registryName, item);
        return item;
    }
}
