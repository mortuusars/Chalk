package io.github.mortuusars.chalk.fabric.client;

import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.client.render.MarkModel;
import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.world.chalk.MarkSet;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FabricMarkModel extends WrapperBlockStateModel implements FabricBlockStateModel, MarkModel {
    private final ModelBaker.Interner interner;

    public FabricMarkModel(BlockStateModel baseModel, ModelBaker.Interner interner) {
        super(baseModel);
        this.interner = interner;
    }

    @Override
    public Object createGeometryKey(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random) {
        return new GeometryKey(super.createGeometryKey(level, pos, state, random), getMarkEntries(level, pos));
    }

    @Override
    public void emitQuads(@NonNull QuadEmitter emitter, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state,
                          @NonNull RandomSource random, @NonNull Predicate<Direction> cullTest) {
        super.emitQuads(emitter, level, pos, state, random, cullTest);

        if (level.getBlockEntity(pos) instanceof MarkBlockEntity blockEntity) {
            MarkSet marks = blockEntity.getMarks();
            marks.forEach((direction, mark) -> {
                emitter.fromBakedQuad(bakeMarkQuad(interner, direction, mark));
                emitter.shadeMode(ShadeMode.VANILLA).emit();
            });
        }
    }

    private static List<MarkEntry> getMarkEntries(BlockAndTintGetter level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof MarkBlockEntity blockEntity)) {
            return List.of();
        }

        List<MarkEntry> entries = new ArrayList<>();
        blockEntity.getMarks().forEach((direction, mark) -> entries.add(new MarkEntry(direction.get3DDataValue(), mark)));
        return List.copyOf(entries);
    }

    private record GeometryKey(Object baseKey, List<MarkEntry> marks) {
    }

    private record MarkEntry(int directionIndex, Mark mark) {
    }
}
