package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.menus.ChalkBoxMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final RegistryObject<MenuType<ChalkBoxMenu>> CHALK_BOX = Registry.MENUS.register("chalk_box",
            () -> IForgeMenuType.create(ChalkBoxMenu::fromBuffer));

    public static void register() {}
}
