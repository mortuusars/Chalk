package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.render.ChalkMarkBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class StartupClient {

    @SubscribeEvent
    public static void onModelBakeEvent(ModelBakeEvent event){

        for (BlockState blockState : ModBlocks.WHITE_CHALK_MARK_BLOCK.get().getStateDefinition().getPossibleStates()){
            ModelResourceLocation variantMRL = BlockModelShapes.stateToModelLocation(blockState);
            IBakedModel existingModel = event.getModelRegistry().get(variantMRL);

            if (existingModel == null) {
                Chalk.LOGGER.warn("Did not find the expected vanilla baked model(s) for ChalkMarkBlock in registry");
            } else if (existingModel instanceof ChalkMarkBakedModel) {
                Chalk.LOGGER.warn("Tried to replace ChalkMarkBakedModel twice");
            } else {
                ChalkMarkBakedModel customModel = new ChalkMarkBakedModel(existingModel);
                event.getModelRegistry().put(variantMRL, customModel);
            }
        }
    }

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(ModBlocks.WHITE_CHALK_MARK_BLOCK.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModBlocks.RED_CHALK_MARK_BLOCK.get(), RenderType.cutout());
    }

    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        if (event.getMap().location() == AtlasTexture.LOCATION_BLOCKS) {
            event.addSprite(ChalkMarkBakedModel.centerTextureRL);
            event.addSprite(ChalkMarkBakedModel.arrowTextureRL);
        }
    }
}
