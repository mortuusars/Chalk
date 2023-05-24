package io.github.mortuusars.chalk.utils;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.block.ChalkMarkBlock;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.core.SymbolOrientation;
import io.github.mortuusars.chalk.core.SymbolUnlocking;
import io.github.mortuusars.chalk.data.Lang;
import io.github.mortuusars.chalk.network.Packets;
import io.github.mortuusars.chalk.network.packet.ClientboundSelectSymbolPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MarkDrawingContext {
    @Nullable
    private static MarkDrawingContext storedContext;

    private final Player player;
    private final Level level;
    private final BlockHitResult hitResult;
    private final InteractionHand drawingHand;
    private final SymbolOrientation initialOrientation;

    public MarkDrawingContext(Player player, @NotNull BlockHitResult hitResult, InteractionHand drawingHand) {
        this.player = player;
        this.level = player.level;
        this.hitResult = hitResult;
        this.drawingHand = drawingHand;
        this.initialOrientation = SymbolOrientation.fromClickLocationAll(hitResult.getLocation(), hitResult.getDirection());
    }

    public static void storeContext(MarkDrawingContext context) {
        storedContext = context;
    }

    public static @Nullable MarkDrawingContext getStoredContext() {
        return storedContext;
    }

    public static void clearStoredContext() {
        storedContext = null;
    }

    public boolean canDraw() {
        return canBeDrawnOn(hitResult.getBlockPos(), getMarkFacing(), level);
    }

    public Player getPlayer() {
        return player;
    }

    public BlockPos getMarkBlockPos() {
        return hitResult.getBlockPos().relative(getMarkFacing());
    }

    @NotNull
    public Direction getMarkFacing() {
        return hitResult.getDirection();
    }

    public SymbolOrientation getInitialOrientation() {
        return initialOrientation;
    }

    public InteractionHand getDrawingHand() {
        return drawingHand;
    }

    public void openSymbolSelectionScreen() {
        if (level.isClientSide) {
            storeContext(this);
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            List<MarkSymbol> unlockedSymbols = SymbolUnlocking.getUnlockedSymbols(serverPlayer);

            if (unlockedSymbols.size() > 0)
                Packets.sendToClient(new ClientboundSelectSymbolPacket(unlockedSymbols), serverPlayer);
            else
                player.displayClientMessage(Lang.MESSAGE_NO_SYMBOLS_UNLOCKED.translate().withStyle(ChatFormatting.RED), true);
        }
    }

    public Mark createRegularMark(DyeColor color, boolean glowing) {
        return createMark(color, getInitialOrientation() == SymbolOrientation.CENTER ? MarkSymbol.CENTER : MarkSymbol.ARROW, glowing);
    }

    public Mark createMark(DyeColor color, MarkSymbol symbol, boolean glowing) {
        Direction face = getMarkFacing();
        MarkSymbol.OrientationBehavior rotBehavior = symbol.getOrientationBehavior();

        SymbolOrientation orientation;

        if (rotBehavior == MarkSymbol.OrientationBehavior.FULL)
            orientation = initialOrientation;
        else if (rotBehavior == MarkSymbol.OrientationBehavior.CARDINAL)
            orientation = SymbolOrientation.fromClickLocationCardinal(hitResult.getLocation(), face);
        else if (rotBehavior == MarkSymbol.OrientationBehavior.UP_DOWN_CARDINAL && (face == Direction.UP || face == Direction.DOWN))
            orientation = SymbolOrientation.fromRotation(player.getDirection().getOpposite().get2DDataValue() * 90);
        else
            orientation = symbol.getDefaultOrientation();

        return new Mark(face, color, symbol, orientation, glowing);
    }

    public boolean hasExistingMark() {
        return level.getBlockState(hitResult.getBlockPos().relative(getMarkFacing())).getBlock() instanceof ChalkMarkBlock;
    }

    public boolean shouldMarkReplaceAnother(Mark mark) {
        BlockState oldMarkState = level.getBlockState(hitResult.getBlockPos().relative(getMarkFacing()));
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
        return MarkDrawHelper.draw(player, level, getMarkBlockPos(), mark, drawingHand);
    }

    private static boolean canBeDrawnOn(BlockPos pos, Direction face, Level level) {
        BlockState blockStateAtPos = level.getBlockState(pos);
        BlockState markPosState = level.getBlockState(pos.relative(face));
        return (markPosState.isAir() || markPosState.getBlock() instanceof ChalkMarkBlock) &&
                Block.isFaceFull(blockStateAtPos.getCollisionShape(level, pos), face) && !blockStateAtPos.is(Chalk.Tags.Blocks.CHALK_CANNOT_DRAW_ON);
    }
}
