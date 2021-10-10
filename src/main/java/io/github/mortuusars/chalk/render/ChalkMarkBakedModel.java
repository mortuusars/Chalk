package io.github.mortuusars.chalk.render;

import com.sun.media.sound.ModelTransform;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.client.model.b3d.B3DModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;
import net.minecraftforge.common.model.TransformationHelper;
import org.antlr.v4.runtime.atn.SemanticContext;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.CallbackI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static net.minecraftforge.client.model.SimpleModelTransform.IDENTITY;

public class ChalkMarkBakedModel implements IBakedModel {

    public static ModelProperty<Integer> ORIENTATION = new ModelProperty<>();
    public static ModelProperty<Direction> FACING = new ModelProperty<>();
    public static ModelProperty<Boolean> GLOWING = new ModelProperty<>();

    public static final ResourceLocation centerTextureRL = new ResourceLocation("chalk:/block/white_mark_center");
    public static final ResourceLocation arrowTextureRL = new ResourceLocation("chalk:/block/white_mark");

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
        ModelDataMap modelDataMap = builder.build();
        return modelDataMap;
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        int orientation = state.getValue(ChalkMarkBlock.ORIENTATION);
        Direction facing = state.getValue(ChalkMarkBlock.FACING);
        boolean glowing = state.getValue(ChalkMarkBlock.GLOWING);
        ModelDataMap modelDataMap = getEmptyModelData();
        modelDataMap.setData(ORIENTATION, orientation);
        modelDataMap.setData(FACING, facing);
        modelDataMap.setData(GLOWING, glowing);
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
            return _baseModel.getQuads(state, side, rand);
        }

        if (!extraData.hasProperty(FACING)){
            Chalk.LOGGER.error("IModelData did not have expected property: FACING");
            return _baseModel.getQuads(state, side, rand);
        }

        if (!extraData.hasProperty(GLOWING)){
            Chalk.LOGGER.error("IModelData did not have expected property: GLOWING");
            return _baseModel.getQuads(state, side, rand);
        }

//        if (!extraData.getData(ORIENTATION).isPresent() || !extraData.getData(FACING).isPresent())
//            return _baseModel.getQuads(state, side, rand);

        int orientation = extraData.getData(ORIENTATION);
        Direction facing = extraData.getData(FACING);
        boolean isGlowing = extraData.getData(GLOWING);

        List<BakedQuad> quads = new ArrayList<BakedQuad>();

        BakedQuad quad = getQuadByFacing(facing, orientation);

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

    private BakedQuad getQuadByFacing(Direction facing, int orientation) {
        switch (facing) {
            case DOWN:
                return getBakedQuad(facing, orientation, new Vector3f(0, 15.9f, 0), new Vector3f(16, 16, 16), 180);
            case UP:
                return getBakedQuad(facing, orientation, new Vector3f(0, 0, 0), new Vector3f(16, 0.1f, 16), 0);
            case NORTH:
                return getBakedQuad(facing, orientation, new Vector3f(0, 0, 15.9f), new Vector3f(16, 16, 16), 0);
            case SOUTH:
                return getBakedQuad(facing, orientation, new Vector3f(0, 0, 0), new Vector3f(16, 16, 0.1f), 0);
            case WEST:
                return getBakedQuad(facing, orientation, new Vector3f(15.9f, 0, 0), new Vector3f(16, 16, 16), 0);
            case EAST:
                return getBakedQuad(facing, orientation, new Vector3f(0, 0, 0), new Vector3f(0.1f, 16, 16), 0);
            default:
                return getBakedQuad(facing, orientation, new Vector3f(0, 0, 0), new Vector3f(16, 0.1f, 16), 0);
        }
    }

    private BakedQuad getBakedQuad(Direction facing, int orientation,Vector3f from, Vector3f to, int uvRotation) {
        AtlasTexture blocksStitchedTextures = ModelLoader.instance().getSpriteMap().getAtlas(AtlasTexture.LOCATION_BLOCKS);
        TextureAtlasSprite texture;

        if (orientation == 4)
            texture = blocksStitchedTextures.getSprite(centerTextureRL);
        else
            texture = blocksStitchedTextures.getSprite(arrowTextureRL);

        final float[] uvs = new float[]{0f, 0f, 16f, 16f};


        int rotation = rotationFromOrientation(orientation);

        if (facing == Direction.NORTH || facing == Direction.WEST)
            rotation = 360 - rotation;

        // Direction, TintIndex, TextureName(from json), UVs
        BlockPartFace blockPartFace = new BlockPartFace(facing, -1, "", new BlockFaceUV(uvs, uvRotation));

        Direction.Axis rotationAxis = Direction.Axis.Y;

        switch (facing) {
            case DOWN:
            case UP:
                rotationAxis = Direction.Axis.Y;
                break;
            case NORTH:
            case SOUTH:
                rotationAxis = Direction.Axis.Z;
                break;
            case WEST:
            case EAST:
                rotationAxis = Direction.Axis.X;
                break;
        }

        // Origin, Axis, RotationAngle(22.5), Rescale
        BlockPartRotation blockPartRotation = new BlockPartRotation(new Vector3f(0.5f,0.5f,0.5f), rotationAxis, (float)rotation, false);

        // From pos, To pos, Face, Texture, Facing, Transform, Rotation, Shading, Dummy RL
        BakedQuad quad = _faceBakery.bakeQuad(from, to, blockPartFace, texture, facing, IDENTITY,
                blockPartRotation, true, new ResourceLocation("chalk_mark_" + facing));

        return quad;
    }

    private int rotationFromOrientation(int orientation){

        if (orientation < 0 || orientation > 8)
            throw new IllegalArgumentException("Orientation should be 0-8. Passed: " + orientation);

        // Yes. Hardcoded.
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

    // UNUSED

    // IBakedModel method
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState p_200117_1_, @Nullable Direction p_200117_2_, Random p_200117_3_) {
        throw new AssertionError("IBakedModel::getQuads should never be called, only IForgeBakedModel::getQuads");
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
