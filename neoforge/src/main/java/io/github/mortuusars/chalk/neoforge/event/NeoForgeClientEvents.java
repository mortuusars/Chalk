package io.github.mortuusars.chalk.neoforge.event;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.ChalkClient;
import io.github.mortuusars.chalk.client.gui.screens.ChalkBoxScreen;
import io.github.mortuusars.chalk.client.render.MarkBlockColor;
import io.github.mortuusars.chalk.client.ChalkBoxGlowingProperty;
import io.github.mortuusars.chalk.client.ChalkBoxTintSource;
import io.github.mortuusars.chalk.neoforge.client.NeoForgeMarkModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;

import java.util.Map;

@EventBusSubscriber(modid = Chalk.ID, value = Dist.CLIENT)
public class NeoForgeClientEvents {
    @SubscribeEvent
    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ChalkClient::init);
    }

    @SubscribeEvent
    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Chalk.MenuTypes.CHALK_BOX.get(), ChalkBoxScreen::new);
    }

    @SubscribeEvent
    public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Chalk.resource("chalk_box_chalk_color"), ChalkBoxTintSource.CODEC);
    }

    @SubscribeEvent
    private static void registerSelectItemModelProperties(RegisterSelectItemModelPropertyEvent event) {
        event.register(Chalk.resource("glowing"), ChalkBoxGlowingProperty.TYPE);
    }

    @SubscribeEvent
    private static void modelBake(ModelEvent.ModifyBakingResult event) {
        Map<BlockState, BlockStateModel> models = event.getBakingResult().blockStateModels();
        for (BlockState blockState : Chalk.Blocks.MARK.get().getStateDefinition().getPossibleStates()) {
            BlockStateModel existingModel = models.get(blockState);

            if (existingModel instanceof NeoForgeMarkModel)
                Chalk.LOGGER.warn("Tried to replace {} model twice", blockState);
            else if (existingModel != null) {
                NeoForgeMarkModel customModel = new NeoForgeMarkModel(existingModel);
                models.put(blockState, customModel);
            } else
                Chalk.LOGGER.warn("Model for {} not found. Mark model support will not be added for this blockstate.", blockState);
        }
    }

    @SubscribeEvent
    private static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(MarkBlockColor.allFaces(), Chalk.Blocks.MARK.get());
    }
}
