package io.github.mortuusars.chalk.event;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.client.gui.ChalkBoxScreen;
import io.github.mortuusars.chalk.items.ChalkBoxItem;
import io.github.mortuusars.chalk.items.ChalkItem;
import io.github.mortuusars.chalk.render.ChalkMarkBakedModel;
import io.github.mortuusars.chalk.render.ChalkMarkBlockColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Chalk.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(Chalk.Menus.CHALK_BOX.get(), ChalkBoxScreen::new);

            ItemProperties.register(Chalk.Items.CHALK_BOX.get(), ChalkBoxItem.SELECTED_PROPERTY,
                    (stack, level, entity, damage) -> Chalk.Items.CHALK_BOX.get().getSelectedChalkColor(stack));
        });
    }

    @SubscribeEvent
    public static void onModel(ModelEvent.ModifyBakingResult event) {
        Chalk.Blocks.MARKS.forEach((color, block) -> {
            for (BlockState blockState : block.get().getStateDefinition().getPossibleStates()) {
                ModelResourceLocation variantMRL = BlockModelShaper.stateToModelLocation(blockState);
                BakedModel existingModel = event.getModels().get(variantMRL);

                if (existingModel instanceof ChalkMarkBakedModel)
                    Chalk.LOGGER.warn("Tried to replace " + block + " model twice");
                else if (existingModel != null) {
                    ChalkMarkBakedModel customModel = new ChalkMarkBakedModel(existingModel);
                    event.getModels().put(variantMRL, customModel);
                }
                else
                    Chalk.LOGGER.warn(variantMRL + " model not found. ChalkMarkBakedModel would not be added for this blockstate.");
            }
        });

    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event){
        event.register(new ChalkMarkBlockColor(), Chalk.Blocks.MARKS.values()
                .stream()
                .map(RegistryObject::get)
                .toArray(Block[]::new));
    }

    @SubscribeEvent
    public static void onCreativeTabsBuild(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            for (RegistryObject<ChalkItem> item : Chalk.Items.CHALKS.values()) {
                event.accept(item.get());
            }
            event.accept(Chalk.Items.CHALK_BOX.get());
        }
    }
}
