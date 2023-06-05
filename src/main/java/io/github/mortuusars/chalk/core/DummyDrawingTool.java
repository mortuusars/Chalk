package io.github.mortuusars.chalk.core;

import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class DummyDrawingTool implements IDrawingTool {
    @Override
    public Mark getMark(ItemStack itemInHand, MarkDrawingContext drawingContext, MarkSymbol symbol) {
        return null;
    }

    @Override
    public int getMarkColorValue(ItemStack stack) {
        return -1;
    }

    @Override
    public Optional<DyeColor> getMarkColor(ItemStack stack) {
        return Optional.empty();
    }

    @Override
    public boolean getGlowing(ItemStack stack) {
        return false;
    }

    @Override
    public void onMarkDrawn(Player player, InteractionHand hand, BlockPos markBlockPos, BlockState markBlockState) {

    }
}
