package io.github.mortuusars.chalk.fabric;

import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.ChalkClient;
import io.github.mortuusars.chalk.client.ChalkBoxGlowingProperty;
import io.github.mortuusars.chalk.client.ChalkBoxTintSource;
import io.github.mortuusars.chalk.client.gui.screens.ChalkBoxScreen;
import io.github.mortuusars.chalk.client.render.MarkBlockColor;
import io.github.mortuusars.chalk.fabric.client.FabricMarkModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class ChalkFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(pluginContext -> pluginContext.modifyBlockModelAfterBake().register((model, context) ->
              context.state().is(Chalk.Blocks.MARK.get())
                    ? new FabricMarkModel(model, context.baker().interner())
                    : model));

        ChalkClient.init();

        ConfigScreenFactoryRegistry.INSTANCE.register(Chalk.ID, ConfigurationScreen::new);

        ItemTintSources.ID_MAPPER.put(Chalk.resource("chalk_box_chalk_color"), ChalkBoxTintSource.CODEC);
        SelectItemModelProperties.ID_MAPPER.put(Chalk.resource("glowing"), ChalkBoxGlowingProperty.TYPE);

        BlockColorRegistry.register(MarkBlockColor.allFaces(), Chalk.Blocks.MARK.get());

        MenuScreens.register(Chalk.MenuTypes.CHALK_BOX.get(), ChalkBoxScreen::new);
    }
}
