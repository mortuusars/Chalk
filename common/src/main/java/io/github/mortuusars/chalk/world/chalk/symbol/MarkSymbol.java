package io.github.mortuusars.chalk.world.chalk.symbol;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Chalk;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.*;
import net.minecraft.core.registries.codec.RegistryFileCodec;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

import static io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol.OrientationBehavior.*;

public record MarkSymbol(Identifier texture, int rotationOffset, OrientationBehavior orientationBehavior,
                         String group, int groupPriority, Optional<Identifier> requiredAdvancement, boolean supporterOnly) {

    public static final String GROUP_PRIMARY = "primary";
    public static final ResourceKey<MarkSymbol> ARROW =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("arrow"));
    public static final ResourceKey<MarkSymbol> DOT =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("dot"));
    public static final ResourceKey<MarkSymbol> BACK =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("back"));

    public static final String GROUP_SYMBOLS = "symbols";
    public static final ResourceKey<MarkSymbol> CHECK =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("check"));
    public static final ResourceKey<MarkSymbol> CROSS =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("cross"));
    public static final ResourceKey<MarkSymbol> HOUSE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("house"));
    public static final ResourceKey<MarkSymbol> HOOK =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("hook"));
    public static final ResourceKey<MarkSymbol> BOTTLE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("bottle"));
    public static final ResourceKey<MarkSymbol> HEART =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("heart"));
    public static final ResourceKey<MarkSymbol> SKULL =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("skull"));
    public static final ResourceKey<MarkSymbol> FACE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("face"));
    public static final ResourceKey<MarkSymbol> NOTE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("note"));
    public static final ResourceKey<MarkSymbol> SUN =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("sun"));
    public static final ResourceKey<MarkSymbol> MOON =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("moon"));
    public static final ResourceKey<MarkSymbol> STAR =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("star"));
    public static final ResourceKey<MarkSymbol> GRASS =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("grass"));

    public static final ResourceKey<MarkSymbol> CAT =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("cat"));
    public static final ResourceKey<MarkSymbol> DOG =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("dog"));
    public static final ResourceKey<MarkSymbol> DINO =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("dino"));
    public static final ResourceKey<MarkSymbol> CROWN =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("crown"));
    public static final ResourceKey<MarkSymbol> COIN =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("coin"));
    public static final ResourceKey<MarkSymbol> GEM =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("gem"));
    public static final ResourceKey<MarkSymbol> CAKE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("cake"));
    public static final ResourceKey<MarkSymbol> LAMBDA =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("lambda"));
    public static final ResourceKey<MarkSymbol> FLAME =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("flame"));
    public static final ResourceKey<MarkSymbol> SMIRK =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("smirk"));

    public static final String GROUP_TOOLS = "tools";
    public static final ResourceKey<MarkSymbol> PICKAXE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("pickaxe"));
    public static final ResourceKey<MarkSymbol> AXE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("axe"));
    public static final ResourceKey<MarkSymbol> SHOVEL =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("shovel"));
    public static final ResourceKey<MarkSymbol> SWORD =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("sword"));
    public static final ResourceKey<MarkSymbol> HOE =
          ResourceKey.create(Chalk.Registries.MARK_SYMBOL, Chalk.resource("hoe"));

    public static final ResourceKey<MarkSymbol> DEFAULT = DOT;

    // --

    public static final Codec<MarkSymbol> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
          Identifier.CODEC.fieldOf("texture").forGetter(MarkSymbol::texture),
          Codec.intRange(-180, 180).optionalFieldOf("rotation_offset", 0).forGetter(MarkSymbol::rotationOffset),
          OrientationBehavior.CODEC.optionalFieldOf("orientation_behavior", FULL).forGetter(MarkSymbol::orientationBehavior),
          Codec.STRING.optionalFieldOf("group", "symbols").forGetter(MarkSymbol::group),
          Codec.INT.optionalFieldOf("group_priority", 9999).forGetter(MarkSymbol::groupPriority),
          Identifier.CODEC.optionalFieldOf("required_advancement").forGetter(MarkSymbol::requiredAdvancement),
          Codec.BOOL.optionalFieldOf("supporter_only", false).forGetter(MarkSymbol::supporterOnly)
    ).apply(i, MarkSymbol::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MarkSymbol> DIRECT_STREAM_CODEC = StreamCodec.of(
          (buffer, symbol) -> {
              Identifier.STREAM_CODEC.encode(buffer, symbol.texture());
              ByteBufCodecs.INT.encode(buffer, symbol.rotationOffset());
              OrientationBehavior.STREAM_CODEC.encode(buffer, symbol.orientationBehavior());
              ByteBufCodecs.STRING_UTF8.encode(buffer, symbol.group());
              ByteBufCodecs.INT.encode(buffer, symbol.groupPriority());
              ByteBufCodecs.optional(Identifier.STREAM_CODEC).encode(buffer, symbol.requiredAdvancement());
              ByteBufCodecs.BOOL.encode(buffer, symbol.supporterOnly());
          },
          buffer -> new MarkSymbol(
                Identifier.STREAM_CODEC.decode(buffer),
                ByteBufCodecs.INT.decode(buffer),
                OrientationBehavior.STREAM_CODEC.decode(buffer),
                ByteBufCodecs.STRING_UTF8.decode(buffer),
                ByteBufCodecs.INT.decode(buffer),
                ByteBufCodecs.optional(Identifier.STREAM_CODEC).decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer)
          )
    );

    public static final Codec<Holder<MarkSymbol>> CODEC =
          RegistryFileCodec.create(Chalk.Registries.MARK_SYMBOL, DIRECT_CODEC, false);

    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<MarkSymbol>> STREAM_CODEC =
          ByteBufCodecs.holder(Chalk.Registries.MARK_SYMBOL, DIRECT_STREAM_CODEC);

    // --

    public static Optional<Holder.Reference<MarkSymbol>> get(HolderLookup.Provider registries, ResourceKey<MarkSymbol> key) {
        return registries.get(key);
    }

    public static Holder<MarkSymbol> getOrThrow(HolderLookup.Provider registries, ResourceKey<MarkSymbol> key) {
        return get(registries, key).orElseThrow();
    }

//    public static Holder<MarkSymbol> withFallback(HolderLookup.Provider registries, Optional<Holder<MarkSymbol>> symbol) {
//        HolderLookup.RegistryLookup<MarkSymbol> registry = registries.lookupOrThrow(Chalk.Registries.MARK_SYMBOL);
//        registry.
//        return symbol
//              .or(() -> registry.get(DEFAULT))
//              .or(registry::getAny)
//              .orElseThrow();
//    }

    public static Stream<Holder<MarkSymbol>> getAllHolders(HolderLookup.Provider lookup, boolean includeSupporterOnly) {
        Stream<Holder<MarkSymbol>> stream = lookup.lookupOrThrow(Chalk.Registries.MARK_SYMBOL)
              .listElements()
              .map(ref -> ref);
        return includeSupporterOnly
              ? stream
              : stream.filter(symbol -> !symbol.value().supporterOnly());
    }

    // --

    public static void bootstrap(BootstrapContext<MarkSymbol> context) {
        register(context, ARROW, 0, FULL, GROUP_PRIMARY, 10, Optional.empty(), false);
        register(context, DOT, 0, FIXED, GROUP_PRIMARY, 20, Optional.empty(), false);
        register(context, BACK, 0, CARDINAL, GROUP_PRIMARY, 30, adv("chalk:adventure/this_way"), false);
        register(context, CHECK, 45, UP_DOWN_CARDINAL, GROUP_PRIMARY, 40, Optional.empty(), false);
        register(context, CROSS, 45, FIXED, GROUP_PRIMARY, 50, Optional.empty(), false);

        register(context, HOUSE, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 100, adv("chalk:adventure/home_is_where_the_bed_is"), false);
        register(context, HEART, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 110, adv("minecraft:husbandry/tame_an_animal"), false);
        register(context, HOOK, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 120, adv("minecraft:husbandry/fishy_business"), false);
        register(context, BOTTLE, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 130, adv("minecraft:nether/brew_potion"), false);
        register(context, SKULL, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 140, adv("minecraft:adventure/sniper_duel"), false);
        register(context, FACE, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 150, adv("minecraft:adventure/summon_iron_golem"), false);
        register(context, NOTE, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 160, adv("minecraft:adventure/play_jukebox_in_meadows"), false);
        register(context, SUN, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 200, adv("chalk:adventure/consumed_by_the_light"), false);
        register(context, MOON, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 210, adv("chalk:adventure/alone_in_the_darkness"), false);
        register(context, STAR, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 220, adv("chalk:adventure/guiding_star"), false);
        register(context, GRASS, 0, CARDINAL, GROUP_SYMBOLS, 230, adv("chalk:adventure/paint_the_grass"), false);

        register(context, CAT, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 910, Optional.empty(), true);
        register(context, DOG, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 920, Optional.empty(), true);
        register(context, DINO, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 930, Optional.empty(), true);
        register(context, CROWN, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 940, Optional.empty(), true);
        register(context, COIN, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 950, Optional.empty(), true);
        register(context, GEM, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 960, Optional.empty(), true);
        register(context, CAKE, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 980, Optional.empty(), true);
        register(context, LAMBDA, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 990, Optional.empty(), true);
        register(context, FLAME, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 1000, Optional.empty(), true);
        register(context, SMIRK, 0, UP_DOWN_CARDINAL, GROUP_SYMBOLS, 1010, Optional.empty(), true);

        register(context, SWORD, 0, CARDINAL, GROUP_TOOLS, 10, adv("minecraft:story/iron_tools"), false);
        register(context, SHOVEL, 0, CARDINAL, GROUP_TOOLS, 20, adv("minecraft:story/iron_tools"), false);
        register(context, PICKAXE, 0, CARDINAL, GROUP_TOOLS, 30, adv("minecraft:story/iron_tools"), false);
        register(context, AXE, 0, CARDINAL, GROUP_TOOLS, 40, adv("minecraft:story/iron_tools"), false);
        register(context, HOE, 0, CARDINAL, GROUP_TOOLS, 50, adv("minecraft:story/iron_tools"), false);
    }

    static Optional<Identifier> adv(String id) {
        return Optional.of(Identifier.parse(id));
    }

    static void register(BootstrapContext<MarkSymbol> context, ResourceKey<MarkSymbol> key,
                         int rotationOffset, OrientationBehavior behavior, String group, int groupPriority,
                         Optional<Identifier> requiredAdvancement, boolean supporterOnly) {
        Identifier texture = key.identifier().withPrefix("block/mark/");
        context.register(key, new MarkSymbol(texture, rotationOffset, behavior, group, groupPriority, requiredAdvancement, supporterOnly));
    }

    // --

    public enum OrientationBehavior implements StringRepresentable {
        FIXED("fixed"),
        FULL("full"),
        CARDINAL("cardinal"),
        UP_DOWN_CARDINAL("up_down_cardinal");

        public static final Codec<OrientationBehavior> CODEC = StringRepresentable.fromEnum(OrientationBehavior::values);
        public static final StreamCodec<ByteBuf, OrientationBehavior> STREAM_CODEC = ByteBufCodecs.idMapper(
              ByIdMap.continuous(OrientationBehavior::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP), OrientationBehavior::ordinal);

        private final String name;

        OrientationBehavior(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}

