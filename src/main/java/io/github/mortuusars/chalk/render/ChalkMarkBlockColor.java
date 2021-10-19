package io.github.mortuusars.chalk.render;

import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ChalkMarkBlockColor implements BlockColor {
    @Override
    public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int index) {
        if ( !(blockState.getBlock() instanceof ChalkMarkBlock) )
            return 0xffffff;

        return ((ChalkMarkBlock)blockState.getBlock()).getColor().getTextColor();
    }
}
