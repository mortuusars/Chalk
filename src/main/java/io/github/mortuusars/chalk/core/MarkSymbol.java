package io.github.mortuusars.chalk.core;


import io.github.mortuusars.chalk.Chalk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum MarkSymbol implements StringRepresentable {
    CENTER("center", OrientationBehavior.FIXED, SymbolOrientation.NORTH),
    ARROW("arrow", OrientationBehavior.FULL, SymbolOrientation.NORTH),
    CROSS("cross", OrientationBehavior.FIXED, SymbolOrientation.NORTHEAST),
    CHECKMARK("check", OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),
    SKULL("skull", OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),
    HOUSE("house", OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),
    HEART("heart", OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH);

    private final String name;
    private final ResourceLocation textureLocation;
    private final OrientationBehavior orientationBehavior;
    private final SymbolOrientation defaultOrientation;

    MarkSymbol(String name, OrientationBehavior orientationBehavior, SymbolOrientation defaultOrientation) {
        this.name = name;
        this.textureLocation = Chalk.resource("block/mark/" + name);
        this.orientationBehavior = orientationBehavior;
        this.defaultOrientation = defaultOrientation;
    }

    public static MarkSymbol byNameOrDefault(String name) {
        for (MarkSymbol symbol : values()) {
            if (symbol.name.equals(name))
                return symbol;
        }

        return CENTER;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public OrientationBehavior getOrientationBehavior() {
        return orientationBehavior;
    }

    public SymbolOrientation getDefaultOrientation() {
        return defaultOrientation;
    }

    public String getTranslationKey() {
        return "gui." + Chalk.ID + ".symbol." + name;
    }

    public enum OrientationBehavior {
        FIXED,
        FULL,
        CARDINAL,
        UP_DOWN_CARDINAL,
    }
}
