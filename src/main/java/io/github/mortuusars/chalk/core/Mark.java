package io.github.mortuusars.chalk.core;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

public record Mark(Direction facing, DyeColor color, MarkSymbol symbol, SymbolOrientation orientation, boolean glowing) {
    public BlockState createBlockState() {
        return Chalk.Blocks.getMarkBlock(color).defaultBlockState()
                .setValue(ChalkMarkBlock.FACING, facing())
                .setValue(ChalkMarkBlock.SYMBOL, symbol())
                .setValue(ChalkMarkBlock.ORIENTATION, orientation())
                .setValue(ChalkMarkBlock.GLOWING, glowing());
    }

    public static Mark fromBuffer(FriendlyByteBuf buffer) {
        return new Mark(
                buffer.readEnum(Direction.class),
                buffer.readEnum(DyeColor.class),
                buffer.readEnum(MarkSymbol.class),
                buffer.readEnum(SymbolOrientation.class),
                buffer.readBoolean()
        );
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(facing);
        buffer.writeEnum(color);
        buffer.writeEnum(symbol);
        buffer.writeEnum(orientation);
        buffer.writeBoolean(glowing);
    }
}
