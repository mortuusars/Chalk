package io.github.mortuusars.chalk.utils;

import com.mojang.math.Vector3f;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.block.ChalkMarkBlock;
import io.github.mortuusars.chalk.core.DummyDrawingTool;
import io.github.mortuusars.chalk.core.IDrawingTool;
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
    public static boolean draw(Player player, Level level, BlockPos markPos, BlockState markBlockState, int markColor, InteractionHand drawingHand) {
        ItemStack drawingItemstack = player.getItemInHand(drawingHand);
        IDrawingTool drawingTool = drawingItemstack.getItem() instanceof IDrawingTool tool ? tool : new DummyDrawingTool();

        boolean isMarkDrawn = level.setBlock(markPos, markBlockState, Block.UPDATE_ALL_IMMEDIATE);

        if (isMarkDrawn) {
            if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
                drawingTool.getMarkColor(drawingItemstack).ifPresent(color -> {
                    BlockPos surfacePos = markPos.relative(markBlockState.getValue(ChalkMarkBlock.FACING).getOpposite());
                    BlockState surfaceState = player.level.getBlockState(surfacePos);
                    MaterialColor surfaceMaterialColor = surfaceState.getMapColor(player.level, surfacePos);
                    Chalk.CriteriaTriggers.CHALK_DRAW_COLORS.trigger(serverPlayer, surfaceMaterialColor, color);
                });

                float R = (markColor & 0x00FF0000) >> 16;
                float G = (markColor & 0x0000FF00) >> 8;
                float B = (markColor & 0x000000FF);

                Vector3f pos = PositionUtils.blockCenterOffsetToFace(markPos, markBlockState.getValue(ChalkMarkBlock.FACING), 0.25f);

                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(R / 255, G / 255, B / 255), 2f),
                        pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
                serverLevel.playSound(null, pos.x(), pos.y(), pos.z(), Chalk.SoundEvents.MARK_DRAW.get(),
                        SoundSource.BLOCKS, 0.7f,  new Random().nextFloat() * 0.2f + 0.8f);

                if (markBlockState.getValue(ChalkMarkBlock.GLOWING)) {
                    serverLevel.playSound(null, markPos, Chalk.SoundEvents.GLOWING.get(), SoundSource.BLOCKS, 0.8f, 1f);
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            pos.x(), pos.y(), pos.z(), 1, 0, 0, 0, 0);
                }
            }

            drawingTool.onMarkDrawn(player, drawingHand, markPos, markBlockState);
            player.swing(drawingHand);
        }

        return isMarkDrawn;
    }
}
