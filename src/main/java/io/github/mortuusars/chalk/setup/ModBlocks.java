package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.Blocks.ChalkMarkBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ModBlocks {
    public static final RegistryObject<ChalkMarkBlock> CHALK_MARK_BLOCK =
            Registry.BLOCKS.register("chalk_mark_block",
                    () -> new ChalkMarkBlock(AbstractBlock.Properties.of(Material.REPLACEABLE_FIREPROOF_PLANT)
                            .instabreak()
                            .noCollission()
                            .noOcclusion()
                            .sound(SoundType.GRAVEL)));

    public static void register(){}

    public static void processContentClientSide(final FMLClientSetupEvent event)
    {
        RenderTypeLookup.setRenderLayer(CHALK_MARK_BLOCK.get(), RenderType.cutout());
    }
}
