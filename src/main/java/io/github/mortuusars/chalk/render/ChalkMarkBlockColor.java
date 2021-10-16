package io.github.mortuusars.chalk.render;

import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nullable;

public class ChalkMarkBlockColor implements IBlockColor {
    @Override
    public int getColor(BlockState blockState, @Nullable IBlockDisplayReader blockDisplayReader, @Nullable BlockPos blockPos, int index) {
        if ( !(blockState.getBlock() instanceof ChalkMarkBlock) )
            return 0xffffff;

        return ((ChalkMarkBlock)blockState.getBlock()).getColor().getColorValue();
    }
}
