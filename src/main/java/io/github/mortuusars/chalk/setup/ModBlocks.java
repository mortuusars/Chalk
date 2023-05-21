package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;

public class ModBlocks {
    public static final HashMap<String, RegistryObject<ChalkMarkBlock>> MARKS = new HashMap<String, RegistryObject<ChalkMarkBlock>>();

    public static final RegistryObject<ChalkMarkBlock> BLACK_CHALK_MARK_BLOCK = createColoredMark("black_chalk_mark", DyeColor.BLACK, MaterialColor.COLOR_BLACK);
    public static final RegistryObject<ChalkMarkBlock> RED_CHALK_MARK_BLOCK = createColoredMark("red_chalk_mark", DyeColor.RED, MaterialColor.COLOR_RED);
    public static final RegistryObject<ChalkMarkBlock> GREEN_CHALK_MARK_BLOCK = createColoredMark("green_chalk_mark", DyeColor.GREEN, MaterialColor.COLOR_GREEN);
    public static final RegistryObject<ChalkMarkBlock> BROWN_CHALK_MARK_BLOCK = createColoredMark("brown_chalk_mark", DyeColor.BROWN, MaterialColor.COLOR_BROWN);
    public static final RegistryObject<ChalkMarkBlock> BLUE_CHALK_MARK_BLOCK = createColoredMark("blue_chalk_mark", DyeColor.BLUE, MaterialColor.COLOR_BLUE);
    public static final RegistryObject<ChalkMarkBlock> PURPLE_CHALK_MARK_BLOCK = createColoredMark("purple_chalk_mark", DyeColor.PURPLE, MaterialColor.COLOR_PURPLE);
    public static final RegistryObject<ChalkMarkBlock> CYAN_CHALK_MARK_BLOCK = createColoredMark("cyan_chalk_mark", DyeColor.CYAN, MaterialColor.COLOR_CYAN);
    public static final RegistryObject<ChalkMarkBlock> LIGHT_GRAY_CHALK_MARK_BLOCK = createColoredMark("light_gray_chalk_mark", DyeColor.LIGHT_GRAY, MaterialColor.COLOR_LIGHT_GRAY);
    public static final RegistryObject<ChalkMarkBlock> GRAY_CHALK_MARK_BLOCK = createColoredMark("gray_chalk_mark", DyeColor.GRAY, MaterialColor.COLOR_GRAY);
    public static final RegistryObject<ChalkMarkBlock> PINK_CHALK_MARK_BLOCK = createColoredMark("pink_chalk_mark", DyeColor.PINK, MaterialColor.COLOR_PINK);
    public static final RegistryObject<ChalkMarkBlock> LIME_CHALK_MARK_BLOCK = createColoredMark("lime_chalk_mark", DyeColor.LIME, MaterialColor.GRASS);
    public static final RegistryObject<ChalkMarkBlock> YELLOW_CHALK_MARK_BLOCK = createColoredMark("yellow_chalk_mark", DyeColor.YELLOW, MaterialColor.COLOR_YELLOW);
    public static final RegistryObject<ChalkMarkBlock> LIGHT_BLUE_CHALK_MARK_BLOCK = createColoredMark("light_blue_chalk_mark", DyeColor.LIGHT_BLUE, MaterialColor.COLOR_LIGHT_BLUE);
    public static final RegistryObject<ChalkMarkBlock> MAGENTA_CHALK_MARK_BLOCK = createColoredMark("magenta_chalk_mark", DyeColor.MAGENTA, MaterialColor.COLOR_MAGENTA);
    public static final RegistryObject<ChalkMarkBlock> ORANGE_CHALK_MARK_BLOCK = createColoredMark("orange_chalk_mark", DyeColor.ORANGE, MaterialColor.COLOR_ORANGE);
    public static final RegistryObject<ChalkMarkBlock> WHITE_CHALK_MARK_BLOCK = createColoredMark("white_chalk_mark", DyeColor.WHITE, MaterialColor.SNOW);

    public static void register(){}

    public static ChalkMarkBlock getMarkBlockByColor(DyeColor color){
        String key = color.toString() + "_chalk_mark";
        return MARKS.get(key).get();
    }

    private static RegistryObject<ChalkMarkBlock> createColoredMark(String registryName, DyeColor dyeColor, MaterialColor materialColor){
        RegistryObject<ChalkMarkBlock> registeredBlock = Registry.BLOCKS.register(registryName,
                () -> new ChalkMarkBlock(dyeColor, BlockBehaviour.Properties.of(Material.REPLACEABLE_FIREPROOF_PLANT, materialColor)
                        .instabreak()
                        .emissiveRendering((pState, pLevel, pPos) -> pState.getValue(ChalkMarkBlock.GLOWING))
                        .noOcclusion()
                        .noCollission()
                        .sound(SoundType.GRAVEL)));
        MARKS.put(registryName, registeredBlock);
        return registeredBlock;
    }
}
