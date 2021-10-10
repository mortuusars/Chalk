package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraftforge.fml.RegistryObject;


import java.util.*;

public class ModBlocks {

    public static final HashMap<String, RegistryObject<ChalkMarkBlock>> MARKS = new HashMap<String, RegistryObject<ChalkMarkBlock>>();

    public static final RegistryObject<ChalkMarkBlock> WHITE_CHALK_MARK_BLOCK = createColoredMark("white_chalk_mark", DyeColor.WHITE, MaterialColor.SNOW);
    public static final RegistryObject<ChalkMarkBlock> RED_CHALK_MARK_BLOCK = createColoredMark("red_chalk_mark", DyeColor.RED, MaterialColor.COLOR_RED);

    public static void register(){}

    public static ChalkMarkBlock getMarkBlockByColor(DyeColor color){
        String key = color.toString() + "_chalk_mark";
        return MARKS.get(key).get();
    }

    private static RegistryObject<ChalkMarkBlock> createColoredMark(String registryName, DyeColor dyeColor, MaterialColor materialColor){
        RegistryObject<ChalkMarkBlock> registeredBlock = Registry.BLOCKS.register(registryName,
                () -> new ChalkMarkBlock(dyeColor, AbstractBlock.Properties.of(Material.REPLACEABLE_FIREPROOF_PLANT, materialColor)
                        .instabreak()
                        .noOcclusion()
                        .noCollission()
                        .sound(SoundType.GRAVEL)));
        MARKS.put(registryName, registeredBlock);
        return registeredBlock;
    }
}
