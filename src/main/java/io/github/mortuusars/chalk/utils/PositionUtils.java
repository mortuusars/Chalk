package io.github.mortuusars.chalk.utils;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class PositionUtils {
    /**
     * Returns coords of a center of BlockPos
     */
    public static Vector3f blockCenter(BlockPos blockPos){
        return new Vector3f(blockPos.getX() + 0.5f, blockPos.getY() + 0.5f, blockPos.getZ() + 0.5f);
    }

    /**
     * Returns coords from a center of BlockPos with offset (from center) to one of the faces.
     */
    public static Vector3f blockCenterOffsetToFace(BlockPos blockPos, Direction facing, float offset){
        Vector3f vec = blockCenter(blockPos);

        Vec3i normal = facing.getNormal();
        return new Vector3f(vec.x() - (normal.getX() * offset), vec.y() - (normal.getY() * offset), vec.z() - (normal.getZ() * offset));
    }
}
