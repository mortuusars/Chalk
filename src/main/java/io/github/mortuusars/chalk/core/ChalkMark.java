package io.github.mortuusars.chalk.core;

import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.blocks.MarkSymbol;
import io.github.mortuusars.chalk.setup.ModBlocks;
import io.github.mortuusars.chalk.setup.ModTags;
import io.github.mortuusars.chalk.utils.ClickLocationUtils;
import io.github.mortuusars.chalk.utils.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class ChalkMark {
    public static boolean canBeDrawnAt(BlockPos pos, BlockPos clickedBlockPos, Direction clickedFace, Level level){

        BlockState clickedBlockState = level.getBlockState(clickedBlockPos);
        if (clickedBlockState.is(ModTags.Blocks.CHALK_MARK))
            return true; // Marks can be replaced.

        BlockState stateAtMarkPos = level.getBlockState(pos);
        if ( !stateAtMarkPos.is(Blocks.AIR) && !stateAtMarkPos.is(ModTags.Blocks.CHALK_MARK))
            return false;

        return Block.isFaceFull(clickedBlockState.getCollisionShape(level, clickedBlockPos), clickedFace);
    }

    public static BlockState createMarkBlockState(MarkSymbol symbol, DyeColor color, Direction clickedFace, Vec3 clickLocation, BlockPos clickedPos, boolean isGlowing){
        BlockState newBlockState = ModBlocks.getMarkBlockByColor(color).defaultBlockState()
                .setValue(ChalkMarkBlock.FACING, clickedFace)
                .setValue(ChalkMarkBlock.SYMBOL, symbol)
                .setValue(ChalkMarkBlock.GLOWING, isGlowing);

        if (symbol == MarkSymbol.NONE)
            newBlockState = newBlockState.setValue(ChalkMarkBlock.ORIENTATION, ClickLocationUtils.getBlockRegion(clickLocation, clickedPos, clickedFace));

        return newBlockState;
    }

    public static boolean drawMark(BlockState markState, BlockPos markPos, Level level) {
        boolean isMarkDrawn = level.setBlock(markPos, markState, Block.UPDATE_ALL_IMMEDIATE);

        if (isMarkDrawn){
            double pX = markPos.getX() + 0.5;
            double pY = markPos.getY() + 0.5;
            double pZ = markPos.getZ() + 0.5;
            level.playSound(null, pX, pY, pZ, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                    SoundSource.BLOCKS, 0.7f,  new Random().nextFloat() * 0.2f + 0.8f);

            if (level.isClientSide) {
                DyeColor color = ((ChalkMarkBlock) markState.getBlock()).getColor();
                ParticleUtils.spawnColorDustParticles(color, level, markPos, markState.getValue(ChalkMarkBlock.FACING));
            }
        }

        return isMarkDrawn;
    }

    // TODO: move drawing logic here
}
