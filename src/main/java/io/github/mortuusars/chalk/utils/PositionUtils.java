package io.github.mortuusars.chalk.utils;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;

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
    public static Vector3f blockFaceCenter(BlockPos blockPos, Direction facing,  float offset){
        Vector3f vec = blockCenter(blockPos);
        Vector3i normal = facing.getNormal();

        Vector3f finalVec = new Vector3f(vec.x() - (normal.getX() * offset), vec.y() - (normal.getY() * offset), vec.z() - (normal.getZ() * offset));

        return finalVec;
    }
}
