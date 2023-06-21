package io.github.mortuusars.chalk.core;

import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.Config;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class SymbolUnlocking {
    public static List<MarkSymbol> getUnlockedSymbols(ServerPlayer player) {
        List<MarkSymbol> unlocked = new ArrayList<>();

        for (MarkSymbol symbol : MarkSymbol.getSpecialSymbols()) {
            Pair<ForgeConfigSpec.BooleanValue, ForgeConfigSpec.ConfigValue<String>> symbolConfig = Config.SYMBOL_CONFIG.get(symbol);

            if (!symbolConfig.getFirst().get())
                continue;

            String advancementLocation = symbolConfig.getSecond().get();

            if (advancementLocation.isEmpty() || hasAdvancement(player, new ResourceLocation(advancementLocation)))
                unlocked.add(symbol);
        }

        return unlocked;
    }

    private static boolean hasAdvancement(ServerPlayer player, ResourceLocation advancementID) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            Chalk.LOGGER.error("Cannot check advancements: server is null");
            return false;
        }

        Advancement advancement = server.getAdvancements().getAdvancement(advancementID);

        if (advancement == null)
            return false;

        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        return progress.isDone();
    }
}
