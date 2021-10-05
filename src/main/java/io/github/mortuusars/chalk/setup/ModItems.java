package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.items.ChalkItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

public class ModItems {
    public static RegistryObject<ChalkItem> CHALK = Registry.ITEMS.register("chalk", () -> new ChalkItem(new Item.Properties()));

    public static void register(){}
}
