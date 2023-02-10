package io.github.mortuusars.chalk.render;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.setup.ModBlocks;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class Rendering {

    public static final ChalkMarkBlockColor CHALK_MARK_BLOCK_COLOR = new ChalkMarkBlockColor();

    @SubscribeEvent
    public static void onModelBakeEvent(ModelEvent.BakingCompleted event) {
        // Register custom IBakedModel for all mark blocks
        ModBlocks.MARKS.forEach( (name, block) -> {
            for (BlockState blockState : block.get().getStateDefinition().getPossibleStates()) {
                ModelResourceLocation variantMRL = BlockModelShaper.stateToModelLocation(blockState);
                BakedModel existingModel = event.getModelManager().getModel(variantMRL);

                if (existingModel instanceof ChalkMarkBakedModel) {
                    Chalk.LOGGER.warn("Tried to replace " + block + " twice");
                } else {
                    ChalkMarkBakedModel customModel = new ChalkMarkBakedModel(existingModel);
                    event.getModels().put(variantMRL, customModel);
                }
            }
        });
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event){
        BlockColors blockColors = event.getBlockColors();

        ModBlocks.MARKS.forEach((name, block) -> {
            blockColors.register(CHALK_MARK_BLOCK_COLOR, block.get());
        });
    }

    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        // Register textures for use in IBakedModel
        if (event.getAtlas().location() == TextureAtlas.LOCATION_BLOCKS) {
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
            ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout());
        });
    }
}