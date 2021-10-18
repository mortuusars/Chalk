package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.items.ChalkItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.fmllegacy.RegistryObject;

import java.util.HashMap;

public class ModItems {
    public static HashMap<String, RegistryObject<ChalkItem>> CHALKS = new HashMap<String, RegistryObject<ChalkItem>>();

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

    public static void register(){}

    public static ChalkItem getChalkByColor(DyeColor color){
        return CHALKS.get(color.toString() + "_chalk").get();
    }

    private static RegistryObject<ChalkItem> registerColoredChalk(DyeColor dyeColor){
        String registryName = dyeColor.toString() + "_chalk";
        RegistryObject<ChalkItem> item = Registry.ITEMS.register(registryName, () -> new ChalkItem(dyeColor, new Item.Properties()));
        CHALKS.put(registryName, item);
        return item;
    }
}
