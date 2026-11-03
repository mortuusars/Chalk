package io.github.mortuusars.chalk.client.render;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.math.Quadrant;
import io.github.mortuusars.chalk.world.chalk.Mark;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import org.joml.Vector3f;

import java.util.HashMap;

public interface MarkModel {
    Vector3f ROTATION_ORIGIN = new Vector3f(0.5f, 0.5f, 0.5f);
    CuboidFace.UVs UVS = new CuboidFace.UVs(0f, 0f, 16f, 16f);

    HashMap<Direction, Vector3f> FROM_COORDS = Util.make(new HashMap<>(), map -> {
        map.put(Direction.DOWN, new Vector3f(0, 15.95f, 0));
        map.put(Direction.UP, new Vector3f(0, 0, 0));
        map.put(Direction.NORTH, new Vector3f(0, 0, 15.95f));
        map.put(Direction.SOUTH, new Vector3f(0, 0, 0));
        map.put(Direction.WEST, new Vector3f(15.95f, 0, 0));
        map.put(Direction.EAST, new Vector3f(0, 0, 0));
    });
    HashMap<Direction, Vector3f> TO_COORDS = Util.make(new HashMap<>(), map -> {
        map.put(Direction.DOWN, new Vector3f(16, 16, 16));
        map.put(Direction.UP, new Vector3f(16, 0.05f, 16));
        map.put(Direction.NORTH, new Vector3f(16, 16, 16));
        map.put(Direction.SOUTH, new Vector3f(16, 16, 0.05f));
        map.put(Direction.WEST, new Vector3f(16, 16, 16));
        map.put(Direction.EAST, new Vector3f(0.05f, 16, 16));
    });

    default BakedQuad bakeMarkQuad(ModelBaker.Interner interner, Direction direction, Mark mark) {
        TextureAtlasSprite texture = getMarkTexture(mark);

        int tintIndex = direction.get3DDataValue(); // MarkBlockColor tints each face independently.

        // Rotate the texture
        int rotationOffset = mark.symbol().value().rotationOffset();
        int rotation = (mark.orientation().getRotation() + (direction == Direction.DOWN ? -rotationOffset : rotationOffset)) % 360;

        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE || direction.getAxis() == Direction.Axis.Y) {
            rotation = 360 - rotation;
        }

        CuboidRotation blockPartRotation = new CuboidRotation(ROTATION_ORIGIN,
              new CuboidRotation.SingleAxisRotation(direction.getAxis(), rotation), false);

        Vector3f from = FROM_COORDS.get(direction);
        Vector3f to = TO_COORDS.get(direction);
        BakedQuad.MaterialInfo materialInfo = BakedQuad.MaterialInfo.of(new Material.Baked(texture, false),
              Transparency.TRANSPARENT, tintIndex, null, mark.glowing() ? 15 : 0);

        return FaceBakery.bakeQuad(interner, from, to, UVS,
              direction == Direction.DOWN ? Quadrant.R180 : Quadrant.R0, materialInfo, direction,
              BlockModelRotation.IDENTITY, blockPartRotation);
    }

    /**
     * Returns the material flags without constructing and baking the quad.
     * Material flags are queried during outline extraction, which can happen every frame.
     */
    default int markMaterialFlags(Mark mark) {
        Material.Baked material = new Material.Baked(getMarkTexture(mark), false);
        return BakedQuad.MaterialInfo.of(material, Transparency.TRANSPARENT, 0, null,
              mark.glowing() ? 15 : 0).flags();
    }

    @SuppressWarnings("deprecation")
    default TextureAtlasSprite getMarkTexture(Mark mark) {
        // Proper way would probably be to use Sheets.BLOCKS_MAPPER, but mark texture already has "block/" prefix,
        // and changing mark_symbol JSONs would require updating 'Chalk: Chars' and other addons/resourcepacks that have symbols,
        // which will break the neat compatibility with all versions thing.
        return Minecraft.getInstance().getAtlasManager()
              .get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, mark.symbol().value().texture()));
    }
}
