package io.github.mortuusars.chalk.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

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
    public static int getBlockRegion(Vec3 clickLocation, BlockPos pos, Direction facing){
        Pair<Double, Double> point = getClickedBlockSpaceCoords(clickLocation, pos, facing);

        final int xRegion = Math.min(2, (int) (point.getFirst() / 0.333));
        final int yRegion = Math.min(2, (int) (point.getSecond() / 0.333));

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
    public static Pair<Double, Double> getClickedBlockSpaceCoords(Vec3 clickLocation, BlockPos pos, Direction facing) {
        final double x = clickLocation.x - pos.getX();
        final double y = clickLocation.y - pos.getY();
        final double z = clickLocation.z - pos.getZ();

        return switch (facing) {
            case NORTH -> Pair.of(1 - x, 1 - y);
            case SOUTH -> Pair.of(x, 1 - y);
            case WEST -> Pair.of(z, 1 - y);
            case EAST -> Pair.of(1 - z, 1 - y);
            default -> Pair.of(x, z);
        };
    }
}
