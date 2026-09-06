package io.github.mortuusars.chalk.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.ChalkClient;
import io.github.mortuusars.chalk.client.gui.screens.ChalkBoxScreen;
import io.github.mortuusars.chalk.client.render.MarkBlockColor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class ChalkFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ChalkClient.init();

        ConfigScreenFactoryRegistry.INSTANCE.register(Chalk.ID, ConfigurationScreen::new);

        ColorProviderRegistry.ITEM.register((stack, index) ->
              Chalk.Items.CHALK.get().getTintColor(stack, index), Chalk.Items.CHALK.get());
        ColorProviderRegistry.ITEM.register((stack, index) ->
              Chalk.Items.CHALK_BOX.get().getTintColor(stack, index), Chalk.Items.CHALK_BOX.get());
        ColorProviderRegistry.BLOCK.register(new MarkBlockColor(), Chalk.Blocks.MARK.get());

        BlockRenderLayerMap.INSTANCE.putBlock(Chalk.Blocks.MARK.get(), RenderType.cutout());

        MenuScreens.register(Chalk.MenuTypes.CHALK_BOX.get(), ChalkBoxScreen::new);
    }
}
