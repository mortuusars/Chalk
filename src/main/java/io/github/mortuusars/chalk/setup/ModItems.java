package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.items.ChalkItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

import java.util.HashMap;

public class ModItems {

    public static HashMap<String, RegistryObject<ChalkItem>> CHALKS = new HashMap<String, RegistryObject<ChalkItem>>();

    public static RegistryObject<ChalkItem> WHITE_CHALK = registerColoredChalk(DyeColor.WHITE);
    public static RegistryObject<ChalkItem> RED_CHALK = registerColoredChalk(DyeColor.RED);

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
