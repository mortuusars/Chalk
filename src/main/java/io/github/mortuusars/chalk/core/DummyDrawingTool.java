package io.github.mortuusars.chalk.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class DummyDrawingTool implements IDrawingTool {
    @Override
    public Optional<DyeColor> getMarkColor(ItemStack stack) {
        return Optional.empty();
    }

    @Override
    public boolean getGlowing(ItemStack stack) {
        return false;
    }

    @Override
    public void onMarkDrawn(Player player, InteractionHand hand, BlockPos markBlockPos, Mark mark) {

    }
}
