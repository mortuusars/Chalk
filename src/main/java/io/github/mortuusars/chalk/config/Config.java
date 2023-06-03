package io.github.mortuusars.chalk.config;

import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.chalk.core.MarkSymbol;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final ForgeConfigSpec COMMON;
    public static final ForgeConfigSpec.IntValue CHALK_DURABILITY;
    public static final ForgeConfigSpec.IntValue GLOWING_CHALK_MARK_LIGHT_LEVEL;
    public static final ForgeConfigSpec.BooleanValue CHALK_BOX_GLOWING;
    public static final ForgeConfigSpec.IntValue CHALK_BOX_GLOWING_USES;
    public static final ForgeConfigSpec.BooleanValue GENERATE_IN_CHESTS;
    public static final Map<MarkSymbol, Pair<ForgeConfigSpec.BooleanValue, ForgeConfigSpec.ConfigValue<String>>> SYMBOL_CONFIG;

    public static final ForgeConfigSpec CLIENT;
    public static final Map<MarkSymbol, ForgeConfigSpec.IntValue> SYMBOL_ROTATION_OFFSETS;


    static {
        // COMMON

        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        CHALK_DURABILITY = builder.comment("How many marks you can draw with a single chalk. Default: 64")
                                  .defineInRange("ChalkUses", 64, 0, Integer.MAX_VALUE);

        GLOWING_CHALK_MARK_LIGHT_LEVEL = builder.comment("How much light glowing mark produces. Default: 5")
                .defineInRange("GlowingMarkLightLevel", 5, 0, 15);


        CHALK_BOX_GLOWING = builder.comment("Controls whether glowing should be enabled in Chalk Box.\nIf disabled - you will not be able to draw glowing marks with chalk box.\nDefault: true")
                .define("ChalkBoxGlowingEnabled", true);

        CHALK_BOX_GLOWING_USES = builder.comment("How many glowing uses one glowing item will give.\nDefault: 8")
                .defineInRange("ChalkBoxGlowingItemUses", 8, 1, 9999);

        GENERATE_IN_CHESTS = builder.comment("If enabled, Chalks (and Chalk Boxes) will generate in Dungeons, Abandoned Mineshafts, Planes and Savanna villages, Cartographer houses\nDefault: true")
                                    .define("ShouldGenerateInChests", true);

        // SYMBOLS
        builder.comment("Enable/disable symbols and location of the advancement that will unlock that symbol. (Empty = always unlocked)")
               .push("Symbols");

        Map<MarkSymbol, String> symbolAdvancements = new HashMap<>();
        symbolAdvancements.put(MarkSymbol.CHECKMARK, "");
        symbolAdvancements.put(MarkSymbol.CROSS, "");
        symbolAdvancements.put(MarkSymbol.SKULL, "chalk:adventure/get_skeleton_skull");
        symbolAdvancements.put(MarkSymbol.HOUSE, "chalk:adventure/sleep_three_times_in_one_place");
        symbolAdvancements.put(MarkSymbol.HEART, "minecraft:husbandry/tame_an_animal");
        symbolAdvancements.put(MarkSymbol.PICKAXE, "minecraft:story/iron_tools");

        SYMBOL_CONFIG = new HashMap<>();

        for (var entry : symbolAdvancements.entrySet()) {
            MarkSymbol symbol = entry.getKey();
            String advancement = entry.getValue();

            String symbolName = StringUtils.capitalize(symbol.getSerializedName());
            SYMBOL_CONFIG.put(symbol, Pair.of(
                    builder.define(symbolName + "Enabled", true),
                    builder.define(symbolName + "UnlockAdvancement", advancement)));
        }

        builder.pop();

        COMMON = builder.build();


        // CLIENT:

        builder = new ForgeConfigSpec.Builder();

        builder.comment("Rotation offsets (in degrees) for each mark.").push("SymbolOffsets");

        SYMBOL_ROTATION_OFFSETS = new HashMap<>();

        for (MarkSymbol symbol : MarkSymbol.values()) {
            String symbolName = StringUtils.capitalize(symbol.getSerializedName());
            int defaultOffset = symbol == MarkSymbol.CROSS || symbol == MarkSymbol.CHECKMARK ? 45 : 0;
            SYMBOL_ROTATION_OFFSETS.put(symbol, builder.defineInRange(symbolName + "RotationOffset",
                    defaultOffset, -360, 360));
        }

        builder.pop();

        CLIENT = builder.build();
    }
}
