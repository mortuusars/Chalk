package io.github.mortuusars.chalk.render;

import io.github.mortuusars.chalk.block.ChalkMarkBlock;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ChalkMarkBlockColor implements BlockColor {
    @Override
    public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int index) {
        return blockState.getBlock() instanceof ChalkMarkBlock chalkMarkBlock ?
                ChalkColors.fromDyeColor(chalkMarkBlock.getColor())
                : 0xffffff;
    }
}
