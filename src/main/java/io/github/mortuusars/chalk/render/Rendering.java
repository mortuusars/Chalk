package io.github.mortuusars.chalk.render;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.render.ChalkMarkBakedModel;
import io.github.mortuusars.chalk.setup.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class Rendering {

    public static final ChalkMarkBlockColor CHALK_MARK_BLOCK_COLOR = new ChalkMarkBlockColor();

    @SubscribeEvent
    public static void onModelBakeEvent(ModelBakeEvent event) {
        // Register custom IBakedModel for all mark blocks
        ModBlocks.MARKS.forEach( (name, block) -> {
            for (BlockState blockState : block.get().getStateDefinition().getPossibleStates()) {
                ModelResourceLocation variantMRL = BlockModelShapes.stateToModelLocation(blockState);
                IBakedModel existingModel = event.getModelRegistry().get(variantMRL);

                if (existingModel == null) {
                    Chalk.LOGGER.warn("Did not find the expected vanilla baked model(s) for " + block + " in registry");
                } else if (existingModel instanceof ChalkMarkBakedModel) {
                    Chalk.LOGGER.warn("Tried to replace " + block + " twice");
                } else {
                    ChalkMarkBakedModel customModel = new ChalkMarkBakedModel(existingModel);
                    event.getModelRegistry().put(variantMRL, customModel);
                }
            }
        });
    }

    @SubscribeEvent
    public static void registerBlockColors(ColorHandlerEvent.Block event){
        BlockColors blockColors = event.getBlockColors();

        ModBlocks.MARKS.forEach((name, block) -> {
            blockColors.register(CHALK_MARK_BLOCK_COLOR, block.get());
        });
    }

    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        // Register textures for use in IBakedModel
        if (event.getMap().location() == AtlasTexture.LOCATION_BLOCKS) {
            event.addSprite(new ResourceLocation("chalk:block/mark_arrow"));
            event.addSprite(new ResourceLocation("chalk:block/mark_center"));
            event.addSprite(new ResourceLocation("chalk:block/mark_cross"));
        }
    }

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        setRenderLayerForMarks();
    }

    private static void setRenderLayerForMarks() {
        ModBlocks.MARKS.forEach( (name, block) -> {
            RenderTypeLookup.setRenderLayer(block.get(), RenderType.cutout());
        });
    }
}
