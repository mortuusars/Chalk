package io.github.mortuusars.chalk;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Config {
    public static class Server {
        public static final ModConfigSpec SPEC;

        // Chalk
        public static final ModConfigSpec.ConfigValue<List<? extends String>> CHALK_DYE_COLORS_DEFINITION;
        public static final ModConfigSpec.BooleanValue ADD_DYED_CHALKS_TO_TAB;
        public static final ModConfigSpec.BooleanValue DYED_CHALK_NAMES;

        // Chalk Box
        public static final ModConfigSpec.BooleanValue CHALK_BOX_SHOW_DURABILITY_BAR;
        public static final ModConfigSpec.BooleanValue CHALK_BOX_GLOWING_ENABLED;
        public static final ModConfigSpec.IntValue CHALK_BOX_GLOWING_AMOUNT_PER_ITEM;

        public static final ModConfigSpec.BooleanValue SYMBOL_UNLOCKING;
        public static final ModConfigSpec.BooleanValue SYMBOL_UNLOCKING_CHAT_MESSAGE;
        public static final ModConfigSpec.BooleanValue GLOWING_ENABLED;

        public static BiMap<DyeColor, Integer> CHALK_COLORS = ImmutableBiMap.of();

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            {
                builder.push("chalk");
                CHALK_DYE_COLORS_DEFINITION = builder
                      .comment("Defines which color a specific dye would have when applied to a chalk item.")
                      .defineList("chalk_dye_colors", () -> List.of(
                            "white:#FFFFFF",
                            "light_gray:#BDBFBE",
                            "gray:#818285",
                            "black:#2C2D2E",
                            "brown:#BF7B43",
                            "red:#F55A49",
                            "orange:#FF9A3D",
                            "yellow:#FFDB4A",
                            "lime:#AAF048",
                            "green:#78C73C",
                            "cyan:#37DCD2",
                            "light_blue:#95E3FD",
                            "blue:#5583F8",
                            "purple:#C061FF",
                            "magenta:#F85EC4",
                            "pink:#FF87A9"
                      ), () -> "", Server::validateChalkColorsDefinition);

                ADD_DYED_CHALKS_TO_TAB = builder
                      .comment("Colored chalks will be added to creative menu and JEI", "Default: true")
                      .define("add_dyed_chalks_to_tab", true);

                DYED_CHALK_NAMES = builder
                      .comment("Colored chalks will be named after a dye color if only a single dye was used to color it.", "Default: true")
                      .define("dyed_chalk_names", true);

                builder.pop();
            }

            {
                builder.push("chalk_box");
                CHALK_BOX_SHOW_DURABILITY_BAR = builder
                      .comment("Durability bar of selected chalk will be shown on a chalk box.", "Default: true")
                      .define("show_durability_bar", true);

                CHALK_BOX_GLOWING_ENABLED = builder
                      .comment("Controls whether glowing should be enabled in Chalk Box.",
                            "If disabled - you will not be able to draw glowing marks with chalk box.",
                            "Default: true")
                      .define("glowing_enabled", true);

                CHALK_BOX_GLOWING_AMOUNT_PER_ITEM = builder
                      .comment("How many glowing uses one glowing item will give.\nDefault: 8")
                      .defineInRange("amount_per_glowing_item", 8, 1, 9999);
                builder.pop();
            }

            {
                builder.push("symbols");
                SYMBOL_UNLOCKING = builder
                      .comment("Some mark symbols need to be unlocked by completing specific advancements.",
                            "Setting this to 'false' will bypass the unlocking feature, making all symbols always available.",
                            "Default: true")
                      .define("unlocking", true);

                SYMBOL_UNLOCKING_CHAT_MESSAGE = builder
                      .comment("When a symbol is unlocked, message will be shown in the chat.", "Default: true")
                      .define("unlocking_chat_message", true);
                builder.pop();
            }

            GLOWING_ENABLED = builder
                  .comment("Global toggle for the 'glowing' feature. Disabling this setting will prevent creation of new glowing marks. Existing marks will remain.",
                        "Default: true")
                  .define("glowing_enabled", true);

            SPEC = builder.build();
        }

        private static boolean validateChalkColorsDefinition(Object object) {
            if (!(object instanceof String value)) {
                Chalk.LOGGER.error("[{}] is not valid value for chalk_colors.", object);
                return false;
            }

            if (!value.contains(":")) {
                Chalk.LOGGER.error("[{}] is not valid chalk color mapping. Should be in format '<dye>:<hex-color>'.", value);
                return false;
            }

            String[] split = value.trim().split(":");

            if (split.length != 2) {
                Chalk.LOGGER.error("[{}] is not valid chalk color mapping. Should be in format '<dye>:<hex-color>'.", value);
                return false;
            }

            @Nullable DyeColor dyeColor = DyeColor.byName(split[0].trim(), null);

            if (dyeColor == null) {
                Chalk.LOGGER.error("[{}] is not valid chalk color mapping. '{}' is not a known DyeColor.", value, split[0].trim());
                return false;
            }

            try {
                Long.parseLong(split[1].trim().replace("#", ""), 16);
            } catch (NumberFormatException e) {
                Chalk.LOGGER.error("[{}] is not valid chalk color mapping. '{}' is not a valid hex color value.", value, split[1].trim());
                return false;
            }

            return true;
        }

        private static void createColorsMap() {
            BiMap<DyeColor, Integer> map = HashBiMap.create(DyeColor.values().length);

            for (String entry : CHALK_DYE_COLORS_DEFINITION.get()) {
                try {
                    String[] split = entry.trim().split(":");

                    if (split.length == 2 && DyeColor.byName(split[0].trim(), null) instanceof DyeColor dyeColor) {
                        map.forcePut(dyeColor, (int) Long.parseLong(split[1].trim().replace("#", ""), 16));
                    }
                } catch (Exception e) {
                    Chalk.LOGGER.error("Failed to decode chalk color from '{}'.", entry, e);
                }
            }

            CHALK_COLORS = map;
        }

        public static void loading() {
            createColorsMap();
        }

        public static void reloading() {
            createColorsMap();
        }
    }

    public static class Common {
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.BooleanValue LOOT;
        public static final ModConfigSpec.BooleanValue GLOWING_MARK_PARTICLES;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            // This option needs to be in common config as server config is not loaded when value is requested
            LOOT = builder.comment("If enabled, Chalks (and Chalk Boxes) will generate in Dungeons, Abandoned Mineshafts, " +
                              "Villages (Planes and Savanna), Cartographer village houses",
                        "Default: true")
                  .define("generate_chalk_in_loot_chests", true);

            GLOWING_MARK_PARTICLES = builder
                  .comment("Glowing marks will occasionally emit sparkling particles. Default: true")
                  .define("glowing_mark_particles", true);

            SPEC = builder.build();
        }
    }

    public static class Client {
        public static final ModConfigSpec SPEC;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> SYMBOL_SELECTION_GROUP_SORTING;
        public static final ModConfigSpec.BooleanValue CHALK_BOX_TOOLTIP_CONTENTS;
        public static final ModConfigSpec.BooleanValue CHALK_BOX_TOOLTIP_DETAILS;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            SYMBOL_SELECTION_GROUP_SORTING = builder
                  .comment("Defines how groups will be sorted in symbol selection overlay.",
                        "Undefined groups will be sorted alphabetically and placed after defined ones.")
                  .defineList("symbol_selection_groups_sorting", () -> List.of("primary", "symbols", "tools"), () -> "", o -> o instanceof String);

            CHALK_BOX_TOOLTIP_CONTENTS = builder
                  .comment("Contents of the Chalk Box will be shown in item's tooltip.")
                  .define("chalk_box_tooltip_contents", true);

            CHALK_BOX_TOOLTIP_DETAILS = builder
                  .comment("Information about using Chalk Box will be shown in the item's tooltip.")
                  .define("chalk_box_tooltip_details", true);

            SPEC = builder.build();
        }
    }
}
