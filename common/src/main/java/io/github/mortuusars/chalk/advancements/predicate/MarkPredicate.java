package io.github.mortuusars.chalk.advancements.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.mortaar.advancement.predicate.ColorPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record MarkPredicate(Optional<HolderSet<MarkSymbol>> symbols, Optional<ColorPredicate> color, Optional<Boolean> glowing) {
    public static final Codec<MarkPredicate> CODEC = RecordCodecBuilder.create(i -> i.group(
          RegistryCodecs.holderSet(Chalk.Registries.MARK_SYMBOL).optionalFieldOf("symbols").forGetter(MarkPredicate::symbols),
          ColorPredicate.CODEC.optionalFieldOf("color").forGetter(MarkPredicate::color),
          Codec.BOOL.optionalFieldOf("glowing").forGetter(MarkPredicate::glowing)
    ).apply(i, MarkPredicate::new));

    public boolean matches(Mark mark) {
        return (this.symbols.isEmpty() || this.symbols.get().contains(mark.symbol()))
              && (this.color.isEmpty() || this.color.get().matches(mark.color()))
              && (this.glowing.isEmpty() || this.glowing.get() == mark.glowing());
    }

    public static class Builder {
        private @Nullable HolderSet<MarkSymbol> symbols;
        private @Nullable ColorPredicate color;
        private @Nullable Boolean glowing;

        public Builder symbols(HolderSet<MarkSymbol> symbols) {
            this.symbols = symbols;
            return this;
        }

        public Builder color(ColorPredicate color) {
            this.color = color;
            return this;
        }

        public Builder glowing(boolean glowing) {
            this.glowing = glowing;
            return this;
        }

        public MarkPredicate build() {
            return new MarkPredicate(
                  Optional.ofNullable(symbols),
                  Optional.ofNullable(color),
                  Optional.ofNullable(glowing)
            );
        }
    }
}
