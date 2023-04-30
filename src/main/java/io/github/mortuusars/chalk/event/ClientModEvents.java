package io.github.mortuusars.chalk.event;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.client.gui.ChalkBoxScreen;
import io.github.mortuusars.chalk.items.ChalkBoxItem;
import io.github.mortuusars.chalk.render.ChalkMarkBakedModel;
import io.github.mortuusars.chalk.render.ChalkMarkBlockColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Chalk.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
    public static void onModelBakeEvent(ModelEvent.BakingCompleted event) {
        // Register custom IBakedModel for all mark blocks
        Chalk.Blocks.MARKS.forEach((color, block) -> {
            for (BlockState blockState : block.get().getStateDefinition().getPossibleStates()) {
                ModelResourceLocation variantMRL = BlockModelShaper.stateToModelLocation(blockState);
                BakedModel existingModel = event.getModelManager().getModel(variantMRL);

                if (existingModel instanceof ChalkMarkBakedModel) {
                    Chalk.LOGGER.warn("Tried to replace " + block + " model twice");
                } else {
                    ChalkMarkBakedModel customModel = new ChalkMarkBakedModel(existingModel);
                    event.getModels().put(variantMRL, customModel);
                }
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
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        // Register textures for use in IBakedModel
        if (event.getAtlas().location() == TextureAtlas.LOCATION_BLOCKS) {
            for (MarkSymbol symbol : MarkSymbol.values()) {
                event.addSprite(symbol.getTextureLocation());
            }
        }
    }
}
