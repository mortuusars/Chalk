package io.github.mortuusars.chalk.render;

import com.mojang.math.Transformation;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.block.ChalkMarkBlock;
import io.github.mortuusars.chalk.config.Config;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.core.SymbolOrientation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Baked model is used to programmatically create proper Chalk Mark block model.
 * Based on particular blockstate properties it chooses proper block orientation, texture, texture orientation, glowing.
 */
public class ChalkMarkBakedModel implements BakedModel {
    public static final ModelProperty<Direction> FACING = new ModelProperty<>();
    public static final ModelProperty<SymbolOrientation> ORIENTATION = new ModelProperty<>();
    public static final ModelProperty<MarkSymbol> SYMBOL = new ModelProperty<>();
    public static final ModelProperty<Boolean> GLOWING = new ModelProperty<>();

    private static final ResourceLocation MODEL_NAME = Chalk.resource("block/chalk_mark");
    private static final ModelState MODEL_STATE = new SimpleModelState(Transformation.identity(), false);
    private static final Vector3f ROTATION_ORIGIN = new Vector3f(0.5f, 0.5f, 0.5f);
    private static final FaceBakery faceBakery = new FaceBakery();

    private final BakedModel baseModel;

    public ChalkMarkBakedModel(BakedModel baseModel){
        this.baseModel = baseModel;
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(RenderType.cutout());
    }

    @SuppressWarnings("unused")
    public static ModelData getEmptyModelData(){
        return ModelData.builder()
            .with(FACING, Direction.UP)
            .with(SYMBOL, MarkSymbol.CENTER)
            .with(ORIENTATION, SymbolOrientation.NORTH)
            .with(GLOWING, false)
            .build();
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        SymbolOrientation orientation = state.getValue(ChalkMarkBlock.ORIENTATION);
        Direction facing = state.getValue(ChalkMarkBlock.FACING);
        boolean glowing = state.getValue(ChalkMarkBlock.GLOWING);
        MarkSymbol symbol = state.getValue(ChalkMarkBlock.SYMBOL);

        return modelData.derive()
                .with(ORIENTATION, orientation)
                .with(FACING, facing)
                .with(GLOWING, glowing)
                .with(SYMBOL, symbol)
                .build();
    }

    // Forge method
    @SuppressWarnings("ConstantValue")
    @Override
    public @NotNull List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState state, @org.jetbrains.annotations.Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @org.jetbrains.annotations.Nullable RenderType renderType) {
        if (side != null)
            return Collections.emptyList();

        if (!data.has(FACING)){
            Chalk.LOGGER.error("IModelData did not have expected property: FACING");
            return baseModel.getQuads(state, side, rand, data, renderType);
        }

        if (!data.has(SYMBOL)){
            Chalk.LOGGER.error("IModelData did not have expected property: SYMBOL");
            return baseModel.getQuads(state, side, rand, data, renderType);
        }

        if (!data.has(ORIENTATION)){
            Chalk.LOGGER.error("IModelData did not have expected property: ORIENTATION");
            return baseModel.getQuads(state, side, rand, data, renderType);
        }

        if (!data.has(GLOWING)){
            Chalk.LOGGER.error("IModelData did not have expected property: GLOWING");
            return baseModel.getQuads(state, side, rand, data, renderType);
        }

        Direction facing = data.get(FACING);
        MarkSymbol symbol = data.get(SYMBOL);
        SymbolOrientation orientation = data.get(ORIENTATION);
        boolean isGlowing = Boolean.TRUE.equals(data.get(GLOWING));

        if (facing == null || symbol == null || orientation == null)
            return baseModel.getQuads(state, side, rand, data, renderType);

        List<BakedQuad> quads = new ArrayList<>();
        BakedQuad quad = getBakedQuad(facing, symbol, orientation);

        if (isGlowing)
            quad = convertToFullBright(quad);

        quads.add(quad);
        return quads;
    }

    // IBakedModel method
    @Override
    public @NotNull List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState p_235039_, @org.jetbrains.annotations.Nullable Direction facing, @NotNull RandomSource random) {
        return Collections.emptyList();
    }

    private static BakedQuad convertToFullBright(BakedQuad quad) {
        int[] vertexData = quad.getVertices().clone();
        int step = vertexData.length / 4;

        // Set lighting to fullbright on all vertices
        vertexData[6] = 0x00F000F0;
        vertexData[6 + step] = 0x00F000F0;
        vertexData[6 + 2 * step] = 0x00F000F0;
        vertexData[6 + 3 * step] = 0x00F000F0;

        return new BakedQuad(
                vertexData,
                quad.getTintIndex(),
                quad.getDirection(),
                quad.getSprite(),
                false
        );
    }

    private static final HashMap<Direction, Vector3f> FROM_COORDS;
    private static final HashMap<Direction, Vector3f> TO_COORDS;

    static {
        FROM_COORDS = new HashMap<>();
        FROM_COORDS.put(Direction.DOWN, new Vector3f(0, 15.9f, 0));
        FROM_COORDS.put(Direction.UP, new Vector3f(0, 0, 0));
        FROM_COORDS.put(Direction.NORTH, new Vector3f(0, 0, 15.9f));
        FROM_COORDS.put(Direction.SOUTH, new Vector3f(0, 0, 0));
        FROM_COORDS.put(Direction.WEST, new Vector3f(15.9f, 0, 0));
        FROM_COORDS.put(Direction.EAST, new Vector3f(0, 0, 0));

        TO_COORDS = new HashMap<>();
        TO_COORDS.put(Direction.DOWN, new Vector3f(16, 16, 16));
        TO_COORDS.put(Direction.UP, new Vector3f(16, 0.1f, 16));
        TO_COORDS.put(Direction.NORTH, new Vector3f(16, 16, 16));
        TO_COORDS.put(Direction.SOUTH, new Vector3f(16, 16, 0.1f));
        TO_COORDS.put(Direction.WEST, new Vector3f(16, 16, 16));
        TO_COORDS.put(Direction.EAST, new Vector3f(0.1f, 16, 16));
    }

    private BakedQuad getBakedQuad(@NotNull Direction facing, MarkSymbol symbol, SymbolOrientation symbolRotation) {
        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(symbol.getTextureLocation());

        // Tint index is set to 0 (-1 is off) to color the marks with ChalkMarkBlockColor
        BlockElementFace blockPartFace = new BlockElementFace(facing, 0, "",
                new BlockFaceUV(new float[]{0f, 0f, 16f, 16f}, facing == Direction.DOWN ? 180 : 0));

        // Rotate the texture
        int rotationOffset = Config.SYMBOL_ROTATION_OFFSETS.get(symbol).get();
        int rotation = (symbolRotation.getRotation() + (facing == Direction.DOWN ? -rotationOffset : rotationOffset)) % 360;

        if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE || facing.getAxis() == Direction.Axis.Y)
            rotation = 360 - rotation;

        BlockElementRotation blockPartRotation = new BlockElementRotation(ROTATION_ORIGIN,
                facing.getAxis(), rotation, false);

        Vector3f from = FROM_COORDS.get(facing);
        Vector3f to = TO_COORDS.get(facing);
        return faceBakery.bakeQuad(from, to, blockPartFace, texture, facing, MODEL_STATE, blockPartRotation, true, MODEL_NAME);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return baseModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return baseModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return baseModel.isCustomRenderer();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return baseModel.getParticleIcon();
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return baseModel.getOverrides();
    }
}
