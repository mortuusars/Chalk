package io.github.mortuusars.chalk.advancements;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class AdvancementUtils {
    public static boolean hasAdvancement(ServerPlayer player, Identifier advancement) {
        @Nullable AdvancementHolder advancementHolder = player.level().getServer().getAdvancements().get(advancement);
        if (advancementHolder == null) {
            return false;
        }

        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancementHolder);
        return progress.isDone();
    }
}
