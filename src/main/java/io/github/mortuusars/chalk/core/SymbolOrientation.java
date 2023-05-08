package io.github.mortuusars.chalk.core;

import io.github.mortuusars.chalk.core.component.Point2d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public enum SymbolOrientation implements StringRepresentable {
    CENTER(0),
    NORTH(0),
    NORTHEAST(45),
    EAST(90),
    SOUTHEAST(135),
    SOUTH(180),
    SOUTHWEST(225),
    WEST(270),
    NORTHWEST(315);

    private final int rotation;

    SymbolOrientation(int rotation) {
        this.rotation = rotation;
    }

    public int getRotation() {
        return rotation;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase();
    }

    public static SymbolOrientation fromRotation(int degrees) {
        for (SymbolOrientation orientation : values()) {
            if (orientation == CENTER) continue;

            if (orientation.getRotation() == degrees)
                return orientation;
        }

        return CENTER;
    }

    /**
     * Returns the SymbolOrientation depending where on the block was clicked.
     */
    public static SymbolOrientation fromClickLocationAll(Vec3 clickLocation, Direction face) {
        Point2d coords = getClickedBlockSpaceCoords(clickLocation, face);

        final int x = Math.min(2, (int) (coords.x() / 0.333));
        final int y = Math.min(2, (int) (coords.y() / 0.333));

        final SymbolOrientation[][] rotations = new SymbolOrientation[][]{
                new SymbolOrientation[]{NORTHWEST, NORTH, NORTHEAST},
                new SymbolOrientation[]{WEST,      CENTER, EAST},
                new SymbolOrientation[]{SOUTHWEST, SOUTH, SOUTHEAST}
        };

        return rotations[y][x];
    }

    /**
     * Gets the cardinal direction (NORTH, EAST, SOUTH, WEST) from the click location.
     */
    public static SymbolOrientation fromClickLocationCardinal(Vec3 clickLocation, Direction face) {
        Point2d coords = getClickedBlockSpaceCoords(clickLocation, face);

        final double x = 0.5d - coords.x();
        final double y = 0.5d - coords.y();

        final double radians = Math.atan2(y, x);
        double degrees = radians * (180 / Math.PI);
        degrees = (degrees + 270) % 360; // Adjust so the 0 is NORTH.

        int region = (int)((degrees + 45) % 360) / 90;
        return fromRotation(region * 90);
    }

    /**
     * Returns a point representing where on a block face was clicked. 0.0 to 1.0.
     */
    private static Point2d getClickedBlockSpaceCoords(Vec3 location, Direction face) {
        BlockPos pos = new BlockPos(location.x, location.y, location.z);
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
