package io.github.mortuusars.chalk.core;

import com.mojang.datafixers.util.Pair;
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

    public static SymbolOrientation fromClickLocation(Vec3 clickLocation, Direction face) {
        Pair<Double, Double> coords = getClickedBlockSpaceCoords(clickLocation, face);

        final int x = Math.min(2, (int) (coords.getFirst() / 0.333));
        final int y = Math.min(2, (int) (coords.getSecond() / 0.333));

        final SymbolOrientation[][] rotations = new SymbolOrientation[][]{
                new SymbolOrientation[]{NORTHWEST, NORTH, NORTHEAST},
                new SymbolOrientation[]{WEST,      CENTER, EAST},
                new SymbolOrientation[]{SOUTHWEST, SOUTH, SOUTHEAST}
        };

        return rotations[y][x];
    }

    /**
     * Returns a point representing where in a block was clicked. 0.0 to 1.0.
     */
    private static Pair<Double, Double> getClickedBlockSpaceCoords(Vec3 location, Direction face) {
        BlockPos pos = new BlockPos(location.x, location.y, location.z);
        final double x = location.x - pos.getX();
        final double y = location.y - pos.getY();
        final double z = location.z - pos.getZ();

        return switch (face) {
            case NORTH -> Pair.of(1 - x, 1 - y);
            case SOUTH -> Pair.of(x, 1 - y);
            case WEST -> Pair.of(z, 1 - y);
            case EAST -> Pair.of(1 - z, 1 - y);
            default -> Pair.of(x, z);
        };
    }
}
