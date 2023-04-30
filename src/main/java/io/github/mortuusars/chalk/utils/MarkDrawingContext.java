package io.github.mortuusars.chalk.utils;

import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.core.SymbolOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class MarkDrawingContext {

    public boolean canBeDrawn = false;
    private Direction facing;
    public Direction lookDirection = Direction.NORTH;
    public SymbolOrientation orientation = SymbolOrientation.NORTH;

    public MarkDrawingContext(/*@NotNull Player player, BlockPos clickedPos, Direction clickedFace, Vec3 clickLocation*/) {

    }

    public static MarkDrawingContext create(@NotNull Player player, BlockPos clickedPos, Direction clickedFace, Vec3 clickLocation) {
        Level level = player.level;

        MarkDrawingContext drawingContext = new MarkDrawingContext();

        if (!canBeDrawnOn(clickedPos, clickedFace, level))
            return drawingContext;

        drawingContext.canBeDrawn = true;
        drawingContext.facing = clickedFace;
        drawingContext.lookDirection = player.getDirection();
        drawingContext.orientation = SymbolOrientation.fromClickLocation(clickLocation, clickedFace);

        return drawingContext;
    }

    public Mark createMark(DyeColor color, MarkSymbol symbol, boolean glowing) {
        return new Mark(facing, color, symbol, orientation, glowing);
    }

    private static boolean canBeDrawnOn(BlockPos pos, Direction face, Level level) {
        BlockState blockStateAtPos = level.getBlockState(pos);
        BlockState markPosState = level.getBlockState(pos.relative(face));
        return (markPosState.isAir() || markPosState.getBlock() instanceof ChalkMarkBlock) &&
                Block.isFaceFull(blockStateAtPos.getCollisionShape(level, pos), face);
    }
}
