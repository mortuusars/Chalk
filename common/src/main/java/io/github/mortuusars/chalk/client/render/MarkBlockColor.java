package io.github.mortuusars.chalk.client.render;

import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.world.chalk.Mark;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.stream.IntStream;

public final class MarkBlockColor implements BlockTintSource {
    private final int tintIndex;

    public MarkBlockColor(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    public static List<BlockTintSource> allFaces() {
        return IntStream.range(0, 6).mapToObj(MarkBlockColor::new).map(BlockTintSource.class::cast).toList();
    }

    @Override
    public int color(@NonNull BlockState blockState) {
        return 0xFFFFFFFF;
    }

    @Override
    public int colorInWorld(@NonNull BlockState blockState, BlockAndTintGetter level, @NonNull BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MarkBlockEntity blockEntity) {
            Mark mark = blockEntity.getMarks().get(tintIndex);
            if (mark != null) {
                return mark.color();
            }
        }

        return 0xFFFFFFFF;
    }
}
