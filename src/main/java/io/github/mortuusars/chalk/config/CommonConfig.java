package io.github.mortuusars.chalk.config;

import io.github.mortuusars.chalk.Chalk;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class CommonConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue CHALK_DURABILITY;
    public static final ForgeConfigSpec.IntValue GLOWING_CHALK_MARK_LIGHT_LEVEL;

    public static final ForgeConfigSpec.BooleanValue CHALK_BOX_GLOWING;
    public static final ForgeConfigSpec.IntValue CHALK_BOX_GLOWING_USES;

    public static final ForgeConfigSpec.BooleanValue GENERATE_IN_CHESTS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        CHALK_DURABILITY = builder.comment("How many marks you can draw with a single chalk. Default: 64")
                                  .defineInRange("ChalkUses", 64, 0, Integer.MAX_VALUE);

        GLOWING_CHALK_MARK_LIGHT_LEVEL = builder.comment("How many light glowing mark produces. Default: 5")
                .defineInRange("GlowingMarkLightLevel", 5, 0, 15);


        CHALK_BOX_GLOWING = builder.comment("Controls whether glowing should be enabled in Chalk Box.\nIf disabled - you will not be able to draw glowing marks with chalk box.\nDefault: true")
                .define("ChalkBoxGlowingEnabled", true);

        CHALK_BOX_GLOWING_USES = builder.comment("How many glowing marks one glowing item will give.\nDefault: 8")
                .defineInRange("ChalkBoxGlowingItemUses", 8, 1, 9999);

        GENERATE_IN_CHESTS = builder.comment("If enabled, Chalks (and Chalk Boxes) will generate in Dungeons, Abandoned Mineshafts, Planes and Savanna villages, Cartographer houses\nDefault: true")
                                    .define("ShouldGenerateInChests", true);

        SPEC = builder.build();
    }
}
