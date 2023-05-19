package io.github.mortuusars.chalk.core;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class SymbolUnlocking {
    public static List<MarkSymbol> getUnlockedSymbols(ServerPlayer player) {
        List<MarkSymbol> unlocked = new ArrayList<>();

        unlocked.add(MarkSymbol.HOUSE);
        unlocked.add(MarkSymbol.CHECKMARK);
        unlocked.add(MarkSymbol.CROSS);

//        if (hasAdvancement(player, new ResourceLocation("minecraft:husbandry/tame_an_animal")))
            unlocked.add(MarkSymbol.HEART);

        unlocked.add(MarkSymbol.SKULL);

        return unlocked;
    }

    private static boolean hasAdvancement(ServerPlayer player, ResourceLocation advancementID) {
        MinecraftServer server = player.getLevel().getServer();
        Advancement advancement = server.getAdvancements().getAdvancement(advancementID);

        if (advancement == null)
            return false;

        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        return progress.isDone();
    }
}
