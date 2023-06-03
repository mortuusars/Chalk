package io.github.mortuusars.chalk.core;


import io.github.mortuusars.chalk.Chalk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum MarkSymbol implements StringRepresentable {
    CENTER("center", false, OrientationBehavior.FIXED, SymbolOrientation.NORTH),
    ARROW("arrow", false, OrientationBehavior.FULL, SymbolOrientation.NORTH),
    CROSS("cross", true, OrientationBehavior.FIXED, SymbolOrientation.NORTH),
    CHECKMARK("check", true, OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),
    SKULL("skull", true, OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),
    HOUSE("house", true, OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),
    HEART("heart", true, OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),
    PICKAXE("pickaxe", true, OrientationBehavior.UP_DOWN_CARDINAL, SymbolOrientation.NORTH),;

    private final String name;
    private final boolean isSpecial;
    private final ResourceLocation textureLocation;
    private final OrientationBehavior orientationBehavior;
    private final SymbolOrientation defaultOrientation;

    MarkSymbol(String name, boolean isSpecial, OrientationBehavior orientationBehavior, SymbolOrientation defaultOrientation) {
        this.name = name;
        this.isSpecial = isSpecial;
        this.textureLocation = Chalk.resource("block/mark/" + name);
        this.orientationBehavior = orientationBehavior;
        this.defaultOrientation = defaultOrientation;
    }

    public static List<MarkSymbol> getSpecialSymbols() {
        return Arrays.stream(values()).filter(s -> s.isSpecial).collect(Collectors.toList());
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
