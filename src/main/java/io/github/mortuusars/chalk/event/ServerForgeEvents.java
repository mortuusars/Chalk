package io.github.mortuusars.chalk.event;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.Config;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Chalk.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class ServerForgeEvents {
    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void advancementProgress(AdvancementEvent.AdvancementProgressEvent event) {
        Advancement advancement = event.getAdvancement();

        if (advancement == null || event.getProgressType() == AdvancementEvent.AdvancementProgressEvent.ProgressType.REVOKE
                || !event.getAdvancementProgress().isDone())
            return;

        ResourceLocation id = advancement.getId();

        for (var entry : Config.SYMBOL_CONFIG.entrySet()) {
            boolean isEnabled = entry.getValue().getFirst().get();
            String location = entry.getValue().getSecond().get();
            if (isEnabled && !location.isEmpty() && location.equals(id.toString()) && event.getEntity() instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("gui.chalk.unlocked_symbol_message",
                        Component.translatable(entry.getKey().getTranslationKey()).withStyle(Style.EMPTY.withColor(0x53a5df))), false);
                player.playNotifySound(Chalk.SoundEvents.MARK_DRAW.get(), SoundSource.PLAYERS, 1f, 1f);
                return;
            }
        }
    }
}
