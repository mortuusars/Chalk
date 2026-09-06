package io.github.mortuusars.chalk.world.item;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.advancements.AdvancementUtils;
import io.github.mortuusars.chalk.network.packet.clientbound.SelectSymbolAndDrawMarkClientboundPacket;
import io.github.mortuusars.chalk.util.GridCell;
import io.github.mortuusars.chalk.util.PositionUtils;
import io.github.mortuusars.chalk.world.block.MarkBlock;
import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.world.chalk.DrawnMark;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.world.chalk.MarkDrawingContext;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.chalk.world.chalk.symbol.SymbolOrientation;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import io.github.mortuusars.mortaar.util.supporter.Supporters;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

public interface MarkDrawable {
    int getMarkDrawingColor(ItemStack stack);

    default boolean shouldDrawGlowingMark(ItemStack stack) {
        return false;
    }

    default MarkDrawingContext createMarkDrawingContext(Player player, InteractionHand hand, Vec3 clickedLocation,
                                                        BlockPos clickedPos, Direction clickedFace) {
        BlockPos markPos = clickedPos;
        Direction markFacing = clickedFace;

        if (player.level().getBlockState(clickedPos).getBlock() instanceof MarkBlock
              && MarkBlock.getMarkAt(player.level(), clickedLocation) instanceof DrawnMark existingMark) {
            markFacing = existingMark.facing();
        } else {
            markPos = clickedPos.relative(clickedFace);
        }

        return new MarkDrawingContext(hand, clickedLocation, markPos, markFacing);
    }

    default Mark createRegularMark(Player player, MarkDrawingContext context, ItemStack stack) {
        Holder<MarkSymbol> symbol = context.clickedCell() == GridCell.CENTER
              ? MarkSymbol.getOrThrow(player.level().registryAccess(), MarkSymbol.DOT)
              : MarkSymbol.getOrThrow(player.level().registryAccess(), MarkSymbol.ARROW);
        return createMark(player, context, stack, symbol);
    }

    default Mark createMark(Player player, MarkDrawingContext context, ItemStack stack, Holder<MarkSymbol> symbol) {
        Direction facing = context.markFacing();
        GridCell clickedCell = context.clickedCell();

        MarkSymbol.OrientationBehavior orientationBehavior = symbol.value().orientationBehavior();

        SymbolOrientation orientation = switch (orientationBehavior) {
            case FULL -> SymbolOrientation.fromCell(clickedCell);
            case CARDINAL -> SymbolOrientation.fromClickLocationCardinal(context.clickLocation(), facing);
            case UP_DOWN_CARDINAL -> {
                if (facing != Direction.UP && facing != Direction.DOWN) {
                    yield SymbolOrientation.CENTER;
                }

                Direction direction = player.getDirection();
                if (facing == Direction.UP) {
                    direction = direction.getOpposite();
                }

                yield SymbolOrientation.fromRotation(direction.get2DDataValue() * 90);
            }
            case FIXED -> SymbolOrientation.CENTER;
        };

        return new Mark(symbol, getMarkDrawingColor(stack), orientation, shouldDrawGlowingMark(stack));
    }

    default void selectSymbolAndDraw(Player player, MarkDrawingContext context) {
        if (player instanceof ServerPlayer serverPlayer) {
            List<Holder<MarkSymbol>> availableSymbols = MarkSymbol.getAllHolders(
                        serverPlayer.registryAccess(), Supporters.isEligibleForGoldenRewards(player.getUUID()))
                  .filter(holder -> isSymbolAvailable(serverPlayer, context, holder))
                  .toList();

            new SelectSymbolAndDrawMarkClientboundPacket(availableSymbols, context).sendToClient(serverPlayer);
        }
    }

    default boolean isSymbolAvailable(ServerPlayer player, MarkDrawingContext context, Holder<MarkSymbol> symbol) {
        return player.isCreative()
              || !Config.Server.SYMBOL_UNLOCKING.get()
              || symbol.value().requiredAdvancement()
              .map(advancement -> AdvancementUtils.hasAdvancement(player, advancement))
              .orElse(true);
    }

    default boolean canDrawMark(Player player, MarkDrawingContext context) {
        BlockState markPosState = player.level().getBlockState(context.markPos());
        if (!markPosState.isAir() && !(markPosState.getBlock() instanceof MarkBlock)) {
            return false;
        }

        BlockPos surfacePos = context.surfacePos();
        BlockState surfaceBlockState = player.level().getBlockState(surfacePos);

        return Block.isFaceFull(surfaceBlockState.getCollisionShape(player.level(), surfacePos), context.markFacing())
              && !surfaceBlockState.is(Chalk.Tags.Blocks.CHALK_CANNOT_DRAW_ON);
    }

    default @Nullable Mark getExistingMark(Level level, MarkDrawingContext context) {
        return level.getBlockEntity(context.markPos()) instanceof MarkBlockEntity blockEntity
              ? blockEntity.getMarks().get(context.markFacing())
              : null;
    }

    default boolean shouldMarkReplaceAnother(Mark oldMark, Mark newMark) {
        if (!newMark.symbol().equals(oldMark.symbol())) return true;
        if (newMark.orientation() != oldMark.orientation()) return true;
        if (newMark.color() != oldMark.color()) return true;
        return newMark.glowing() && !oldMark.glowing();
    }

    default boolean drawMark(Player player, MarkDrawingContext context, Mark mark) {
        if (getExistingMark(player.level(), context) instanceof Mark existingMark
              && !shouldMarkReplaceAnother(existingMark, mark)) {
            return false;
        }

        if (placeMark(player.level(), context.markPos(), context.markFacing(), mark) instanceof Mark drawnMark) {
            onMarkDrawn(player, context, drawnMark);
            player.swing(context.hand());
            return true;
        }

        return false;
    }

    default @Nullable Mark placeMark(Level level, BlockPos markPos, Direction markFacing, Mark mark) {
        if (!(MarkBlock.getExistingOrPlaceNew(level, markPos) instanceof MarkBlockEntity blockEntity)) {
            return null;
        }

        @Nullable Mark existingMark = blockEntity.getMarks().get(markFacing);
        if (existingMark != null) {
            mark = calculateNewMark(level, existingMark, mark);
        }

        blockEntity.getMarks().set(markFacing, mark);
        blockEntity.marksChanged();
        return mark;
    }

    default Mark calculateNewMark(Level level, Mark oldMark, Mark newMark) {
        if (oldMark.isSameMarkDifferentColors(newMark)) {
            return newMark
                  .lerpColor(oldMark.color(), 0.5f)
                  .withGlowing(oldMark.glowing() || newMark.glowing());
        }
        return newMark;
    }

    default void onMarkDrawn(Player player, MarkDrawingContext context, Mark mark) {
        if (player instanceof ServerPlayer serverPlayer) {
            float R = (mark.color() & 0x00FF0000) >> 16;
            float G = (mark.color() & 0x0000FF00) >> 8;
            float B = (mark.color() & 0x000000FF);

            Vector3f pos = PositionUtils.blockCenterOffsetToFace(context.markPos(), context.markFacing(), 0.25f);

            serverPlayer.serverLevel().sendParticles(new DustParticleOptions(new Vector3f(R / 255, G / 255, B / 255), 2f),
                  pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
            serverPlayer.serverLevel().playSound(null, pos.x(), pos.y(), pos.z(), Chalk.SoundEvents.MARK_DRAWN.get(),
                  SoundSource.BLOCKS, 0.7f, serverPlayer.getRandom().nextFloat() * 0.2f + 0.8f);

            if (mark.glowing()) {
                serverPlayer.serverLevel().playSound(null, context.markPos(),
                      Chalk.SoundEvents.GLOWING.get(), SoundSource.BLOCKS, 0.8f, 1f);
                serverPlayer.serverLevel().sendParticles(ParticleTypes.END_ROD, pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
            }
        }
    }

    // --

    static ItemStack findMatching(Inventory inventory, int color, @Nullable Supplier<ItemStack> fallback) {
        ItemStack anyDrawable = ItemStack.EMPTY;

        for (ItemStack item : inventory.items) {
            if (!(item.getItem() instanceof MarkDrawable drawable)) {
                continue;
            }

            if (drawable.getMarkDrawingColor(item) == color) {
                return item;
            }

            ChalkBoxContents chalkBoxContents = ChalkBoxContents.of(item);
            if (!chalkBoxContents.isEmpty() && chalkBoxContents.items().stream()
                  .anyMatch(s -> s.getItem() instanceof MarkDrawable sDrawable
                        && sDrawable.getMarkDrawingColor(s) == color)) {
                return item;
            }

            if (anyDrawable.isEmpty()) {
                anyDrawable = item;
            }
        }

        if (fallback != null) {
            return fallback.get();
        }

        return anyDrawable;
    }

    static ItemStack findMatching(Inventory inventory, int color) {
        return findMatching(inventory, color, null);
    }
}
