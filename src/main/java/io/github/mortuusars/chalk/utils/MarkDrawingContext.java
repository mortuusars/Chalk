package io.github.mortuusars.chalk.utils;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.core.SymbolOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class MarkDrawingContext {
    @NotNull
    private final Level level;
    @Nullable
    private final Player player;
    private final BlockHitResult hitResult;

    private Boolean canDraw = null;
    private SymbolOrientation initialOrientation;

    public MarkDrawingContext(@NotNull Level level, @Nullable Player player, @NotNull BlockHitResult hitResult) {
        this.level = level;
        this.player = player;
        this.hitResult = hitResult;

        this.initialOrientation = SymbolOrientation.fromClickLocationAll(hitResult.getLocation(), hitResult.getDirection());
    }

    public boolean canDraw() {
        if (canDraw != null)
            return canDraw;

        canDraw = canBeDrawnOn(hitResult.getBlockPos(), hitResult.getDirection(), level);
        return canDraw;
    }

    public SymbolOrientation getInitialOrientation() {
        return initialOrientation;
    }

    public Mark createMark(DyeColor color, MarkSymbol symbol, boolean glowing) {
        Direction face = hitResult.getDirection();
        MarkSymbol.RotationBehavior rotBehavior = symbol.getRotationBehavior();

        SymbolOrientation orientation;

        if (rotBehavior == MarkSymbol.RotationBehavior.FULL)
            orientation = initialOrientation;
        else if (rotBehavior == MarkSymbol.RotationBehavior.HORIZONTAL
                || (rotBehavior == MarkSymbol.RotationBehavior.UP_DOWN_HORIZONTAL && (face == Direction.UP || face == Direction.DOWN)))
            orientation = SymbolOrientation.fromClickLocationCardinal(hitResult.getLocation(), face);
        else
            orientation = symbol.getDefaultRotation();

        return new Mark(face, color, symbol, orientation, glowing);
    }

    public boolean hasExistingMark() {
        return level.getBlockState(hitResult.getBlockPos().relative(hitResult.getDirection())).getBlock() instanceof ChalkMarkBlock;
    }

    public boolean shouldMarkReplaceAnother(Mark mark) {
        BlockState oldMarkState = level.getBlockState(hitResult.getBlockPos().relative(hitResult.getDirection()));
        if (!(oldMarkState.getBlock() instanceof ChalkMarkBlock markBlock))
            return true;

        if (mark.color() != markBlock.getColor())
            return true;
        if (mark.facing() != oldMarkState.getValue(ChalkMarkBlock.FACING))
            return true;
        else if (mark.symbol() != oldMarkState.getValue(ChalkMarkBlock.SYMBOL))
            return true;
        else if (mark.orientation() != oldMarkState.getValue(ChalkMarkBlock.ORIENTATION))
            return true;
        else
            return (mark.glowing() && !oldMarkState.getValue(ChalkMarkBlock.GLOWING));
    }

    public boolean draw(Mark mark) {
        BlockPos markPos = hitResult.getBlockPos().relative(mark.facing());
        boolean isMarkDrawn = level.setBlock(markPos, mark.createBlockState(), Block.UPDATE_ALL_IMMEDIATE);

        if (isMarkDrawn){
            double pX = markPos.getX() + 0.5;
            double pY = markPos.getY() + 0.5;
            double pZ = markPos.getZ() + 0.5;
            level.playSound(null, pX, pY, pZ, Chalk.SoundEvents.MARK_DRAW.get(),
                    SoundSource.BLOCKS, 0.7f,  new Random().nextFloat() * 0.2f + 0.8f);

            if (level.isClientSide)
                ParticleUtils.spawnColorDustParticles(mark.color(), level, markPos, mark.facing());
        }

        return isMarkDrawn;
    }

    private static boolean canBeDrawnOn(BlockPos pos, Direction face, Level level) {
        BlockState blockStateAtPos = level.getBlockState(pos);
        BlockState markPosState = level.getBlockState(pos.relative(face));
        return (markPosState.isAir() || markPosState.getBlock() instanceof ChalkMarkBlock) &&
                Block.isFaceFull(blockStateAtPos.getCollisionShape(level, pos), face);
    }
}
