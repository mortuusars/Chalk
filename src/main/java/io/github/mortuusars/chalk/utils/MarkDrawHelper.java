package io.github.mortuusars.chalk.utils;

import com.mojang.math.Vector3f;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.core.DummyDrawingTool;
import io.github.mortuusars.chalk.core.IDrawingTool;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.render.ChalkColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;

import java.util.Random;

public class MarkDrawHelper {
    public static boolean draw(Player player, Level level, BlockPos markPos, Mark mark, InteractionHand drawingHand) {
        ItemStack drawingItemstack = player.getItemInHand(drawingHand);
        IDrawingTool drawingTool = drawingItemstack.getItem() instanceof IDrawingTool tool ? tool : new DummyDrawingTool();

        boolean isMarkDrawn = level.setBlock(markPos, mark.createBlockState(), Block.UPDATE_ALL_IMMEDIATE);

        if (isMarkDrawn) {
            if (player instanceof ServerPlayer serverPlayer) {
                drawingTool.getMarkColor(drawingItemstack).ifPresent(color -> {
                    BlockPos surfacePos = markPos.relative(mark.facing().getOpposite());
                    BlockState surfaceState = player.level.getBlockState(surfacePos);
                    MaterialColor surfaceMaterialColor = surfaceState.getMapColor(player.level, surfacePos);
                    Chalk.CriteriaTriggers.CHALK_DRAW_COLORS.trigger(serverPlayer, surfaceMaterialColor, color);
                });
            }

            if (level instanceof ServerLevel serverLevel) {
                int colorValue = ChalkColors.fromDyeColor(mark.color());
                float R = (colorValue & 0x00FF0000) >> 16;
                float G = (colorValue & 0x0000FF00) >> 8;
                float B = (colorValue & 0x000000FF);

                Vector3f pos = PositionUtils.blockCenterOffsetToFace(markPos, mark.facing(), 0.25f);

                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(R / 255, G / 255, B / 255), 2f),
                        pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
                serverLevel.playSound(null, pos.x(), pos.y(), pos.z(), Chalk.SoundEvents.MARK_DRAW.get(),
                        SoundSource.BLOCKS, 0.7f,  new Random().nextFloat() * 0.2f + 0.8f);

                if (mark.glowing()) {
                    serverLevel.playSound(null, markPos, Chalk.SoundEvents.GLOWING.get(), SoundSource.BLOCKS, 0.8f, 1f);
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
                }
            }

            drawingTool.onMarkDrawn(player, drawingHand, markPos, mark);
            player.swing(drawingHand);
        }

        return isMarkDrawn;
    }
}
