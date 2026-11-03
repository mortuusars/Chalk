package io.github.mortuusars.chalk.neoforge.client;

import io.github.mortuusars.chalk.client.render.MarkModel;
import io.github.mortuusars.chalk.world.chalk.Mark;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class NeoForgeMarkModel implements DynamicBlockStateModel, MarkModel {
    private static final ModelBaker.Interner IDENTITY_INTERNER = new ModelBaker.Interner() {
        @Override
        public @NonNull Vector3fc vector(@NonNull Vector3fc vector) {
            return vector;
        }

        @Override
        public BakedQuad.@NonNull MaterialInfo materialInfo(BakedQuad.@NonNull MaterialInfo material) {
            return material;
        }

        @Override
        public @NonNull BakedNormals normals(@NonNull BakedNormals normals) {
            return normals;
        }

        @Override
        public @NonNull BakedColors colors(@NonNull BakedColors colors) {
            return colors;
        }
    };

    private final BlockStateModel baseModel;

    public NeoForgeMarkModel(BlockStateModel baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public @Nullable Object createGeometryKey(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
                                              @NotNull BlockState state, @NotNull RandomSource random) {
        return new GeometryKey(baseModel.createGeometryKey(level, pos, state, random), getMarkEntries(level, pos));
    }

    @Override
    public void collectParts(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state,
                             @NotNull RandomSource random, @NotNull List<BlockStateModelPart> parts) {
        List<BlockStateModelPart> baseParts = new ArrayList<>();
        baseModel.collectParts(level, pos, state, random, baseParts);
        parts.addAll(baseParts);

        List<MarkEntry> markEntries = getMarkEntries(level, pos);
        if (markEntries.isEmpty()) {
            return;
        }

        List<BakedQuad> quads = new ArrayList<>();
        for (MarkEntry entry : markEntries) {
            quads.add(bakeMarkQuad(IDENTITY_INTERNER, Direction.from3DDataValue(entry.directionIndex()), entry.mark()));
        }

        if (!quads.isEmpty()) {
            boolean useAmbientOcclusion = baseParts.isEmpty()
                  || baseParts.stream().allMatch(BlockStateModelPart::useAmbientOcclusion);
            parts.add(new MarkPart(quads, baseModel.particleMaterial(), useAmbientOcclusion));
        }
    }

    @Override
    public void collectParts(@NotNull RandomSource random, @NotNull List<BlockStateModelPart> parts) {
        baseModel.collectParts(random, parts);
    }

    @Override
    public int materialFlags() {
        return baseModel.materialFlags();
    }

    @Override
    public int materialFlags(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state) {
        int flags = baseModel.materialFlags(level, pos, state);
        for (MarkEntry entry : getMarkEntries(level, pos)) {
            flags |= markMaterialFlags(entry.mark());
        }
        return flags;
    }

    @Override
    public @NotNull Material.Baked particleMaterial() {
        return baseModel.particleMaterial();
    }

    @Override
    public @NotNull Material.Baked particleMaterial(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
                                                     @NotNull BlockState state) {
        return baseModel.particleMaterial(level, pos, state);
    }

    private static List<MarkEntry> getMarkEntries(BlockAndTintGetter level, BlockPos pos) {
        Mark[] marks = level.getModelData(pos).get(NeoForgeMarkModelData.MARKS);
        if (marks == null || marks.length == 0) {
            return List.of();
        }

        List<MarkEntry> entries = new ArrayList<>();
        for (int i = 0; i < Math.min(marks.length, Direction.values().length); i++) {
            if (marks[i] != null) {
                entries.add(new MarkEntry(i, marks[i]));
            }
        }
        return List.copyOf(entries);
    }

    private record GeometryKey(@Nullable Object baseKey, List<MarkEntry> marks) {
    }

    private record MarkEntry(int directionIndex, Mark mark) {
    }

    private record MarkPart(List<BakedQuad> quads, Material.Baked particleMaterial,
                            boolean useAmbientOcclusion) implements BlockStateModelPart {
        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable Direction direction) {
            return direction == null ? quads : Collections.emptyList();
        }

        @Override
        public int materialFlags() {
            return quads.stream().mapToInt(quad -> quad.materialInfo().flags()).reduce(0, (left, right) -> left | right);
        }
    }
}
