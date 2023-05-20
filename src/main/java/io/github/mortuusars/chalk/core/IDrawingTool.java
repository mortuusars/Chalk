package io.github.mortuusars.chalk.core;

import com.google.common.base.Preconditions;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IDrawingTool {
    void onMarkDrawn(Player player, InteractionHand hand, Mark mark);
    Optional<DyeColor> getMarkColor(ItemStack stack);
    boolean getGlowing(ItemStack stack);

    default MarkDrawingContext createDrawingContext(UseOnContext context) {
        Preconditions.checkArgument(context.getPlayer() != null, "Player should not be null here.");
        return createDrawingContext(context.getPlayer(), context.getClickedPos(), context.getClickLocation(),
                context.getClickedFace(), context.getHand());
    }

    default MarkDrawingContext createDrawingContext(@NotNull Player player, BlockPos clickedPos, Vec3 clickLocation,
                                                    Direction clickedFace, InteractionHand drawingHand) {
        Level level = player.level;
        Direction facing = clickedFace;
        BlockPos surfacePos = clickedPos;

        if (level.getBlockState(surfacePos).getBlock() instanceof ChalkMarkBlock) {
            facing = level.getBlockState(surfacePos).getValue(ChalkMarkBlock.FACING);
            surfacePos = surfacePos.relative(facing.getOpposite());
        }

        BlockHitResult hitResult = new BlockHitResult(clickLocation, facing, surfacePos, false);
        return new MarkDrawingContext(player, hitResult, drawingHand);
    }

    default boolean drawMark(MarkDrawingContext drawingContext, Mark mark) {
        if (drawingContext.hasExistingMark() && !drawingContext.shouldMarkReplaceAnother(mark))
            return false;

        if (drawingContext.draw(mark)) {
            onMarkDrawn(drawingContext.getPlayer(), drawingContext.getDrawingHand(), mark);
            return true;
        }

        return false;
    }
}
