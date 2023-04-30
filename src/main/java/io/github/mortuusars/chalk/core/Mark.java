package io.github.mortuusars.chalk.core;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public record Mark(Direction facing, DyeColor color, MarkSymbol symbol, SymbolOrientation orientation, boolean glowing) {
//    public boolean shouldReplaceExistingMark(BlockState oldMarkState) {
//        if (!(oldMarkState.getBlock() instanceof ChalkMarkBlock markBlock))
//            throw new IllegalStateException("'oldMarkState' should be ChalkMarkBlock.");
//
//        if (color != markBlock.getColor())
//            return true;
//        if (facing != oldMarkState.getValue(ChalkMarkBlock.FACING))
//            return true;
//        else if (symbol != oldMarkState.getValue(ChalkMarkBlock.SYMBOL))
//            return true;
//        else if (orientation != oldMarkState.getValue(ChalkMarkBlock.ORIENTATION))
//            return true;
//        else
//            return (glowing && !oldMarkState.getValue(ChalkMarkBlock.GLOWING));
//    }

    public BlockState createBlockState() {
        return Chalk.Blocks.getMarkBlock(color).defaultBlockState()
                .setValue(ChalkMarkBlock.FACING, facing())
                .setValue(ChalkMarkBlock.SYMBOL, symbol())
                .setValue(ChalkMarkBlock.ORIENTATION, orientation())
                .setValue(ChalkMarkBlock.GLOWING, glowing());
    }
}
