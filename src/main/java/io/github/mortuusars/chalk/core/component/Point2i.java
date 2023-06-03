package io.github.mortuusars.chalk.core.component;

public record Point2i(int x, int y) {
    public static final Point2i ZERO = new Point2i(0, 0);
}
