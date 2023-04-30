package io.github.mortuusars.chalk.core;


import io.github.mortuusars.chalk.Chalk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum MarkSymbol implements StringRepresentable {
    CENTER("center", RotationBehavior.FIXED, SymbolOrientation.NORTH),
    ARROW("arrow", RotationBehavior.FULL, SymbolOrientation.NORTH),
    CROSS("cross", RotationBehavior.FIXED, SymbolOrientation.NORTHEAST),
    CHECKMARK("check", RotationBehavior.UP_DOWN_HORIZONTAL, SymbolOrientation.NORTH),
    SKULL("skull", RotationBehavior.UP_DOWN_HORIZONTAL, SymbolOrientation.NORTH),
    HOUSE("house", RotationBehavior.UP_DOWN_HORIZONTAL, SymbolOrientation.NORTH),
    HEART("heart", RotationBehavior.UP_DOWN_HORIZONTAL, SymbolOrientation.NORTH);

    private final String name;
    private final ResourceLocation textureLocation;
    private final RotationBehavior rotationBehavior;
    private final SymbolOrientation defaultRotation;

    MarkSymbol(String name, RotationBehavior rotationBehavior, SymbolOrientation defaultRotation) {
        this.name = name;
        this.textureLocation = Chalk.resource("block/mark/" + name);
        this.rotationBehavior = rotationBehavior;
        this.defaultRotation = defaultRotation;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public RotationBehavior getRotationBehavior() {
        return rotationBehavior;
    }

    public SymbolOrientation getDefaultRotation() {
        return defaultRotation;
    }

    public enum RotationBehavior {
        FIXED,
        UP_DOWN_HORIZONTAL,
        HORIZONTAL,
        FULL;
    }
}
