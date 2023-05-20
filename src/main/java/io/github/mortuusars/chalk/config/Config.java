package io.github.mortuusars.chalk.config;

import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.chalk.core.MarkSymbol;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue CHALK_DURABILITY;
    public static final ForgeConfigSpec.IntValue GLOWING_CHALK_MARK_LIGHT_LEVEL;

    public static final ForgeConfigSpec.BooleanValue CHALK_BOX_GLOWING;
    public static final ForgeConfigSpec.IntValue CHALK_BOX_GLOWING_USES;

    public static final ForgeConfigSpec.BooleanValue GENERATE_IN_CHESTS;

    public static final Map<MarkSymbol, Pair<ForgeConfigSpec.BooleanValue, ForgeConfigSpec.ConfigValue<String>>> SYMBOL_CONFIG;

    static {
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
        symbolAdvancements.put(MarkSymbol.HOUSE, "minecraft:husbandry/plant_seed");
        symbolAdvancements.put(MarkSymbol.CHECKMARK, "");
        symbolAdvancements.put(MarkSymbol.CROSS, "");
        symbolAdvancements.put(MarkSymbol.HEART, "minecraft:husbandry/tame_an_animal");
        symbolAdvancements.put(MarkSymbol.SKULL, "chalk:adventure/get_skeleton_skull");

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

        SPEC = builder.build();
    }
}
