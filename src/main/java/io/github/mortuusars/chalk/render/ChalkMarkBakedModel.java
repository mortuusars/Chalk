package io.github.mortuusars.chalk.render;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.blocks.MarkSymbol;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static net.minecraftforge.client.model.SimpleModelTransform.IDENTITY;

/**
 * Baked model is used to programmatically create proper Chalk Mark block model.
 * Based on particular blockstate properties it chooses proper block rotation, texture, texture rotation (for arrow mark), glowing.
 */
public class ChalkMarkBakedModel implements IBakedModel {

    private enum TextureType{
        CENTER, ARROW, CROSS
    }

    public static ModelProperty<Integer> ORIENTATION = new ModelProperty<>();
    public static ModelProperty<Direction> FACING = new ModelProperty<>();
    public static ModelProperty<Boolean> GLOWING = new ModelProperty<>();
    public static ModelProperty<MarkSymbol> SYMBOL = new ModelProperty<>();

    private static final FaceBakery _faceBakery = new FaceBakery();
    private final IBakedModel _baseModel;

    public ChalkMarkBakedModel(IBakedModel baseModel){
        _baseModel = baseModel;
    }

    public static ModelDataMap getEmptyModelData(){
        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        builder.withInitial(ORIENTATION, 4);
        builder.withInitial(FACING, Direction.UP);
        builder.withInitial(GLOWING, false);
        builder.withInitial(SYMBOL, MarkSymbol.NONE);
        ModelDataMap modelDataMap = builder.build();
        return modelDataMap;
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        int orientation = state.getValue(ChalkMarkBlock.ORIENTATION);
        Direction facing = state.getValue(ChalkMarkBlock.FACING);
        boolean glowing = state.getValue(ChalkMarkBlock.GLOWING);
        MarkSymbol symbol = state.getValue(ChalkMarkBlock.SYMBOL);

        ModelDataMap modelDataMap = getEmptyModelData();
        modelDataMap.setData(ORIENTATION, orientation);
        modelDataMap.setData(FACING, facing);
        modelDataMap.setData(GLOWING, glowing);
        modelDataMap.setData(SYMBOL, symbol);
        return modelDataMap;
    }

    // Forge method
    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

        if (side != null)
            return Collections.EMPTY_LIST;

        if (!extraData.hasProperty(ORIENTATION)){
            Chalk.LOGGER.error("IModelData did not have expected property: ORIENTATION");
            return _baseModel.getQuads(state, side, rand, extraData);
        }

        if (!extraData.hasProperty(FACING)){
            Chalk.LOGGER.error("IModelData did not have expected property: FACING");
            return _baseModel.getQuads(state, side, rand, extraData);
        }

        if (!extraData.hasProperty(GLOWING)){
            Chalk.LOGGER.error("IModelData did not have expected property: GLOWING");
            return _baseModel.getQuads(state, side, rand, extraData);
        }

        int orientation = extraData.getData(ORIENTATION);
        Direction facing = extraData.getData(FACING);
        boolean isGlowing = extraData.getData(GLOWING);
        MarkSymbol symbol = MarkSymbol.NONE;

        if (extraData.hasProperty(SYMBOL))
            symbol = extraData.getData(SYMBOL);

        List<BakedQuad> quads = new ArrayList<BakedQuad>();

        BakedQuad quad = getQuadByFacing(facing, orientation, symbol);

        if (isGlowing)
            quad = convertToFullBright(quad);

        quads.add(quad);
        return quads;
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

    private BakedQuad getQuadByFacing(Direction facing, int orientation, MarkSymbol symbol) {
        switch (facing) {
            case DOWN:
                return getBakedQuad(facing, symbol, orientation, new Vector3f(0, 15.9f, 0), new Vector3f(16, 16, 16), 180);
            case UP:
                return getBakedQuad(facing, symbol, orientation, new Vector3f(0, 0, 0), new Vector3f(16, 0.1f, 16), 0);
            case NORTH:
                return getBakedQuad(facing, symbol, orientation, new Vector3f(0, 0, 15.9f), new Vector3f(16, 16, 16), 0);
            case SOUTH:
                return getBakedQuad(facing, symbol, orientation, new Vector3f(0, 0, 0), new Vector3f(16, 16, 0.1f), 0);
            case WEST:
                return getBakedQuad(facing, symbol, orientation, new Vector3f(15.9f, 0, 0), new Vector3f(16, 16, 16), 0);
            case EAST:
                return getBakedQuad(facing, symbol, orientation, new Vector3f(0, 0, 0), new Vector3f(0.1f, 16, 16), 0);
            default:
                return getBakedQuad(facing, symbol, orientation, new Vector3f(0, 0, 0), new Vector3f(16, 0.1f, 16), 0);
        }
    }

    private BakedQuad getBakedQuad(Direction facing, MarkSymbol symbol, int orientation, Vector3f from, Vector3f to, int uvRotation) {

        TextureAtlasSprite texture = getTextureForMark(symbol, orientation);

        // Direction, TintIndex, TextureName(from json), UVs
        // Tint index is set to 0 (-1 is off) to color the marks with ChalkMarkBlockColor
        BlockPartFace blockPartFace = new BlockPartFace(facing, 0, "", new BlockFaceUV(new float[]{0f, 0f, 16f, 16f}, uvRotation));

        // Rotate the texture
        int rotation = symbol == MarkSymbol.CROSS ? 45 : rotationFromOrientation(orientation);

        // Flip rotation for this facings
        if (facing == Direction.NORTH || facing == Direction.WEST)
            rotation = 360 - rotation;

        // Origin, Axis, RotationAngle(22.5), Rescale
        BlockPartRotation blockPartRotation = new BlockPartRotation(new Vector3f(0.5f,0.5f,0.5f), facing.getAxis(), (float)rotation, false);

        // From pos, To pos, Face, Texture, Facing, Transform, Rotation, Shading, Dummy RL
        BakedQuad quad = _faceBakery.bakeQuad(from, to, blockPartFace, texture, facing, IDENTITY,
                blockPartRotation, true, new ResourceLocation("chalk:chalk_mark_" + facing));

        return quad;
    }

    private TextureAtlasSprite getTextureForMark(MarkSymbol symbol, int orientation){
        AtlasTexture atlas = ModelLoader.instance().getSpriteMap().getAtlas(AtlasTexture.LOCATION_BLOCKS);

        if (symbol == MarkSymbol.CROSS)
            return atlas.getSprite(new ResourceLocation("chalk:block/mark_cross"));
        else if (orientation == 4)
            return atlas.getSprite(new ResourceLocation("chalk:block/mark_center"));
        else
            return atlas.getSprite(new ResourceLocation("chalk:block/mark_arrow"));
    }

    private int rotationFromOrientation(int orientation){

        if (orientation < 0 || orientation > 8)
            throw new IllegalArgumentException("Orientation should be 0-8. Passed: " + orientation);

        // Yes, hardcoded.
        switch (orientation){
            case 2: return 315;
            case 5: return 270;
            case 8: return 225;
            case 7: return 180;
            case 6: return 135;
            case 3: return 90;
            case 0: return 45;
            default: return 0;
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return _baseModel.getParticleTexture(data);
    }

    // IBakedModel method
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState p_200117_1_, @Nullable Direction p_200117_2_, Random p_200117_3_) {
        return Collections.EMPTY_LIST;
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
    public ItemOverrideList getOverrides() {
        return _baseModel.getOverrides();
    }
}
