package io.github.mortuusars.chalk.render;

import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.blocks.MarkSymbol;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Baked model is used to programmatically create proper Chalk Mark block model.
 * Based on particular blockstate properties it chooses proper block rotation, texture, texture rotation (for arrow mark), glowing.
 */
public class ChalkMarkBakedModel implements BakedModel {

    private enum MarkType {
        CENTER, ARROW, CROSS
    }

    public static final ResourceLocation CROSS = new ResourceLocation("chalk:block/mark_cross");
    public static final ResourceLocation CENTER = new ResourceLocation("chalk:block/mark_center");
    public static final ResourceLocation ARROW = new ResourceLocation("chalk:block/mark_arrow");

    public static ModelProperty<Integer> ORIENTATION = new ModelProperty<>();
    public static ModelProperty<Direction> FACING = new ModelProperty<>();
    public static ModelProperty<Boolean> GLOWING = new ModelProperty<>();
    public static ModelProperty<MarkSymbol> SYMBOL = new ModelProperty<>();

    private static ModelState MODEL_STATE = new SimpleModelState(Transformation.identity(), false);

    private static final FaceBakery _faceBakery = new FaceBakery();
    private final BakedModel _baseModel;

    public ChalkMarkBakedModel(BakedModel baseModel){
        _baseModel = baseModel;
    }

    public static ModelData getEmptyModelData(){
        return ModelData.builder()
            .with(ORIENTATION, 4)
            .with(FACING, Direction.UP)
            .with(GLOWING, false)
            .with(SYMBOL, MarkSymbol.NONE)
            .build();
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        int orientation = state.getValue(ChalkMarkBlock.ORIENTATION);
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
    @Override
    public @NotNull List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState state, @org.jetbrains.annotations.Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @org.jetbrains.annotations.Nullable RenderType renderType) {
        if (side != null)
            return Collections.EMPTY_LIST;

        if (!data.has(ORIENTATION)){
            Chalk.LOGGER.error("IModelData did not have expected property: ORIENTATION");
            return _baseModel.getQuads(state, side, rand, data, renderType);
        }

        if (!data.has(FACING)){
            Chalk.LOGGER.error("IModelData did not have expected property: FACING");
            return _baseModel.getQuads(state, side, rand, data, renderType);
        }

        if (!data.has(GLOWING)){
            Chalk.LOGGER.error("IModelData did not have expected property: GLOWING");
            return _baseModel.getQuads(state, side, rand, data, renderType);
        }

        int orientation = data.get(ORIENTATION);
        Direction facing = data.get(FACING);
        boolean isGlowing = data.get(GLOWING);
        MarkSymbol symbol = MarkSymbol.NONE;

        if (data.has(SYMBOL))
            symbol = data.get(SYMBOL);

        List<BakedQuad> quads = new ArrayList<BakedQuad>();

//        BakedQuad quad = getQuadByFacing(facing, orientation, symbol);
        BakedQuad quad = getBakedQuad(facing, symbol, orientation, facing == Direction.DOWN ? 180 : 0);

        if (isGlowing)
            quad = convertToFullBright(quad);

        quads.add(quad);
        return quads;
    }

    // IBakedModel method
    @Override
    public List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState p_235039_, @org.jetbrains.annotations.Nullable Direction p_235040_, RandomSource p_235041_) {
        return Collections.EMPTY_LIST;
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

//    private BakedQuad getQuadByFacing(Direction facing, int orientation, MarkSymbol symbol) {
//        switch (facing) {
//            case DOWN:
//                return getBakedQuad(facing, symbol, orientation, new Vector3f(0, 15.9f, 0), new Vector3f(16, 16, 16), 180);
//            case UP:
//                return getBakedQuad(facing, symbol, orientation, new Vector3f(0, 0, 0), new Vector3f(16, 0.1f, 16), 0);
//            case NORTH:
//                return getBakedQuad(facing, symbol, orientation, new Vector3f(0, 0, 15.9f), new Vector3f(16, 16, 16), 0);
//            case SOUTH:
//                return getBakedQuad(facing, symbol, orientation, new Vector3f(0, 0, 0), new Vector3f(16, 16, 0.1f), 0);
//            case WEST:
//                return getBakedQuad(facing, symbol, orientation, new Vector3f(15.9f, 0, 0), new Vector3f(16, 16, 16), 0);
//            case EAST:
//                return getBakedQuad(facing, symbol, orientation, new Vector3f(0, 0, 0), new Vector3f(0.1f, 16, 16), 0);
//            default:
//                return getBakedQuad(facing, symbol, orientation, new Vector3f(0, 0, 0), new Vector3f(16, 0.1f, 16), 0);
//        }
//    }

    private static HashMap<Direction, Vector3f> fromCoords;
    private static HashMap<Direction, Vector3f> toCoords;

    static {
        fromCoords = new HashMap<>();
        fromCoords.put(Direction.DOWN, new Vector3f(0, 15.9f, 0));
        fromCoords.put(Direction.UP, new Vector3f(0, 0, 0));
        fromCoords.put(Direction.NORTH, new Vector3f(0, 0, 15.9f));
        fromCoords.put(Direction.SOUTH, new Vector3f(0, 0, 0));
        fromCoords.put(Direction.WEST, new Vector3f(15.9f, 0, 0));
        fromCoords.put(Direction.EAST, new Vector3f(0, 0, 0));

        toCoords = new HashMap<>();
        toCoords.put(Direction.DOWN, new Vector3f(16, 16, 16));
        toCoords.put(Direction.UP, new Vector3f(16, 0.1f, 16));
        toCoords.put(Direction.NORTH, new Vector3f(16, 16, 16));
        toCoords.put(Direction.SOUTH, new Vector3f(16, 16, 0.1f));
        toCoords.put(Direction.WEST, new Vector3f(16, 16, 16));
        toCoords.put(Direction.EAST, new Vector3f(0.1f, 16, 16));
    }

    private BakedQuad getBakedQuad(Direction facing, MarkSymbol symbol, int orientation, int uvRotation) {

        Vector3f from = facing != null ? fromCoords.get(facing) : fromCoords.get(Direction.UP);
        Vector3f to = facing != null ? toCoords.get(facing) : fromCoords.get(Direction.UP);

        TextureAtlasSprite texture = getTextureForMark(symbol, orientation);

        // Direction, TintIndex, TextureName(from json), UVs
        // Tint index is set to 0 (-1 is off) to color the marks with ChalkMarkBlockColor
        BlockElementFace blockPartFace = new BlockElementFace(facing, 0, "", new BlockFaceUV(new float[]{0f, 0f, 16f, 16f}, uvRotation));

        // Rotate the texture
        int rotation = symbol == MarkSymbol.CROSS ? 45 : rotationFromOrientation(orientation);

        // Flip rotation for this facings
        if (facing == Direction.NORTH || facing == Direction.WEST)
            rotation = 360 - rotation;

        // Origin, Axis, RotationAngle(22.5), Rescale
        BlockElementRotation blockPartRotation = new BlockElementRotation(new Vector3f(0.5f,0.5f,0.5f), facing.getAxis(), (float)rotation, false);

        // From pos, To pos, Face, Texture, Facing, Transform, Rotation, Shading, Dummy RL
        BakedQuad quad = _faceBakery.bakeQuad(from, to, blockPartFace, texture, facing, MODEL_STATE,
                blockPartRotation, true, new ResourceLocation("chalk:chalk_mark_" + facing));

        return quad;
    }

//    private BakedQuad getBakedQuad(Direction facing, MarkSymbol symbol, int orientation, Vector3f from, Vector3f to, int uvRotation) {
//
//        TextureAtlasSprite texture = getTextureForMark(symbol, orientation);
//
//        // Direction, TintIndex, TextureName(from json), UVs
//        // Tint index is set to 0 (-1 is off) to color the marks with ChalkMarkBlockColor
//        BlockElementFace blockPartFace = new BlockElementFace(facing, 0, "", new BlockFaceUV(new float[]{0f, 0f, 16f, 16f}, uvRotation));
//
//        // Rotate the texture
//        int rotation = symbol == MarkSymbol.CROSS ? 45 : rotationFromOrientation(orientation);
//
//        // Flip rotation for this facings
//        if (facing == Direction.NORTH || facing == Direction.WEST)
//            rotation = 360 - rotation;
//
//        // Origin, Axis, RotationAngle(22.5), Rescale
//        BlockElementRotation blockPartRotation = new BlockElementRotation(new Vector3f(0.5f,0.5f,0.5f), facing.getAxis(), (float)rotation, false);
//
//        // From pos, To pos, Face, Texture, Facing, Transform, Rotation, Shading, Dummy RL
//        BakedQuad quad = _faceBakery.bakeQuad(from, to, blockPartFace, texture, facing, MODEL_STATE,
//                blockPartRotation, true, new ResourceLocation("chalk:chalk_mark_" + facing));
//
//        return quad;
//    }

    private TextureAtlasSprite getTextureForMark(MarkSymbol symbol, int orientation){
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);

        if (symbol == MarkSymbol.CROSS)
            return atlas.getSprite(CROSS);
        else if (orientation == 4)
            return atlas.getSprite(CENTER);
        else
            return atlas.getSprite(ARROW);
    }

    private int rotationFromOrientation(int orientation){

        if (orientation < 0 || orientation > 8)
            throw new IllegalArgumentException("Orientation should be 0-8. Passed: " + orientation);

        // Yes, hardcoded.
        return switch (orientation) {
            case 2 -> 315;
            case 5 -> 270;
            case 8 -> 225;
            case 7 -> 180;
            case 6 -> 135;
            case 3 -> 90;
            case 0 -> 45;
            default -> 0;
        };
    }


    @Override
    public boolean useAmbientOcclusion() {
        return _baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return _baseModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return _baseModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return _baseModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return _baseModel.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return _baseModel.getOverrides();
    }
}
