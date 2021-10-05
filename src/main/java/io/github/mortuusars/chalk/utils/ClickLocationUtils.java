package io.github.mortuusars.chalk.utils;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.awt.geom.Point2D;

public class ClickLocationUtils {

    /**
     * Gets the part of a block that was clicked.<p>
     * If top left corner result will be 0. Bottom right - 8.<p>
     * If facing is UP orientation will be to positive Z.<p>
     * if facing down orientation is mirrored from UP.<p>
     * |0 1 2|<p>
     * |3 4 5|<p>
     * |6 7 8|<p>
     */
    public static int getBlockRegion(Vector3d clickLocation, BlockPos pos, Direction facing){

        Point2D.Double point = getClickedBlockSpaceCoords(clickLocation, pos, facing);

        final int xRegion = Math.min(2, (int) (point.x / 0.333));
        final int yRegion = Math.min(2, (int) (point.y / 0.333));

        final int[][] blockRegions = new int[][]{
                new int[]{0, 1, 2},
                new int[]{3, 4, 5},
                new int[]{6, 7, 8}
        };

        return blockRegions[yRegion][xRegion];
    }

    /**
     * Returns a point representing where in a block was clicked. 0.0 to 1.0.
     */
    public static Point2D.Double getClickedBlockSpaceCoords(Vector3d clickLocation, BlockPos pos, Direction facing){

        final double x = clickLocation.x - pos.getX();
        final double y = clickLocation.y - pos.getY();
        final double z = clickLocation.z - pos.getZ();

        switch (facing) {
            case NORTH: return new Point2D.Double(1 - x, 1 - y);
            case SOUTH: return new Point2D.Double(x, 1 - y);
            case WEST: return new Point2D.Double(z, 1- y);
            case EAST: return new Point2D.Double(1 - z, 1 - y);
            default: return new Point2D.Double(x, z);
        }
    }
}
