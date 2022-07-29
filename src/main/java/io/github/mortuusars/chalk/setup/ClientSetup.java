package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.client.gui.ChalkBoxScreen;
import io.github.mortuusars.chalk.items.ChalkBoxItem;
import io.github.mortuusars.chalk.render.Rendering;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static final ResourceLocation CHALK_BOX_SELECTED_PROPERTY = new ResourceLocation(Chalk.MOD_ID, "selected");

    private final IEventBus modEventBus;

    public ClientSetup(IEventBus modEventBus){
        this.modEventBus = modEventBus;
    }

    public void registerClientOnlyEvents(){
        modEventBus.register(Rendering.class);
    }

    public static void onClientSetupEvent(FMLClientSetupEvent event){
        event.enqueueWork(ClientSetup::registerChalkBoxPropertyForItemOverrides);
        event.enqueueWork(ClientSetup::registerScreens);
    }

    private static void registerScreens() {
        MenuScreens.register(ModMenus.CHALK_BOX.get(), ChalkBoxScreen::new);
    }

    private static void registerChalkBoxPropertyForItemOverrides(){
        ChalkBoxItem chalkBoxItem = ModItems.CHALK_BOX.get();
        ItemProperties.register(chalkBoxItem, CHALK_BOX_SELECTED_PROPERTY,
                (stack, level, entity, damage) -> chalkBoxItem.getSelectedChalkColor(stack));
    }
}
