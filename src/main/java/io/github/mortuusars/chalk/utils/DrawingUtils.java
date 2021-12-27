package io.github.mortuusars.chalk.utils;

import io.github.mortuusars.chalk.config.CommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class DrawingUtils {
    public static boolean isGlowingItem(Item item){
        ResourceLocation itemRegistryName = item.getRegistryName();

        if (itemRegistryName == null)
            return false;

        for (String itemName : CommonConfig.GLOWING_ITEMS.get()) {
            if (itemRegistryName.toString().equals(itemName))
                return true;
        }

        return false;
    }
}
