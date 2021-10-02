package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.Blocks.ChalkMarkBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;

public class ModBlocks {
    public static final RegistryObject<ChalkMarkBlock> CHALK_MARK_BLOCK =
            Registry.BLOCKS.register("chalk_mark_block",
                    () -> new ChalkMarkBlock(AbstractBlock.Properties.of(Material.LEAVES)));

    public static void register(){}
}
