package io.github.mortuusars.chalk.world.chalk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.chalk.symbol.SymbolOrientation;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.util.ARGB;

import java.util.Objects;

public record Mark(Holder<MarkSymbol> symbol, int color, SymbolOrientation orientation, boolean glowing) {
    public static final Codec<Mark> CODEC = RecordCodecBuilder.create(i -> i.group(
          RegistryFixedCodec.create(Chalk.Registries.MARK_SYMBOL).fieldOf("symbol").forGetter(Mark::symbol),
          Codec.INT.optionalFieldOf("color", 0xFFFFFFFF).forGetter(Mark::color),
          SymbolOrientation.CODEC.optionalFieldOf("orientation", SymbolOrientation.CENTER).forGetter(Mark::orientation),
          Codec.BOOL.optionalFieldOf("glowing", false).forGetter(Mark::glowing)
    ).apply(i, Mark::new));

    public Mark withSymbol(Holder<MarkSymbol> symbol) {
        return new Mark(symbol, color, orientation, glowing);
    }

    public Mark withColor(int color) {
        return new Mark(symbol, color, orientation, glowing);
    }

    public Mark lerpColor(int targetColor, float delta) {
        return withColor(ARGB.srgbLerp(delta, color, targetColor));
    }

    public Mark withOrientation(SymbolOrientation orientation) {
        return new Mark(symbol, color, orientation, glowing);
    }

    public Mark withGlowing(boolean glowing) {
        return new Mark(symbol, color, orientation, glowing);
    }

    public boolean isSameMarkDifferentColors(Mark other) {
        return other.symbol().equals(symbol())
              && other.orientation() == orientation()
              && other.color() != color();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Mark mark = (Mark) object;
        return color == mark.color && orientation == mark.orientation && glowing == mark.glowing && Objects.equals(symbol, mark.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, color, orientation, glowing);
    }
}
