package io.github.mortuusars.chalk.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

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
        return new Vector3f(vec.x() - (facing.getStepX() * offset), vec.y() - (facing.getStepY() * offset), vec.z() - (facing.getStepZ() * offset));
    }

    /**
     * Returns a point representing where on a block face was clicked. 0.0 to 1.0.
     */
    public static Point2d getClickedBlockSpaceCoords(Vec3 location, Direction face) {
        BlockPos pos = BlockPos.containing(location);

        final double x = location.x - pos.getX();
        final double y = location.y - pos.getY();
        final double z = location.z - pos.getZ();

        return switch (face) {
            case NORTH -> new Point2d(1d - x, 1d - y);
            case SOUTH -> new Point2d(x, 1d - y);
            case WEST -> new Point2d(z, 1d - y);
            case EAST -> new Point2d(1d - z, 1d - y);
            default -> new Point2d(x, z);
        };
    }
}
