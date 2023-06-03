package io.github.mortuusars.chalk.event;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.advancement.PlayerSleepInfo;
import io.github.mortuusars.chalk.config.Config;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Chalk.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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

    @SubscribeEvent
    public static void onSleepFinished(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            boolean sleepingLongEnough = serverPlayer.isSleepingLongEnough();
            if (!sleepingLongEnough)
                return;

            List<String> tags = serverPlayer.getTags().stream().toList();

            List<BlockPos> sleepPositions = new ArrayList<>();

            for (String tag : tags) {
                if (tag.startsWith("PlayerSleepInfo")) {
                    serverPlayer.removeTag(tag);

                    String dataStr = tag.replace("PlayerSleepInfo", "");
                    PlayerSleepInfo sleepInfo = PlayerSleepInfo.deserialize(dataStr);
                    sleepPositions = new ArrayList<>(sleepInfo.sleepPositions());
                    break;
                }
            }

            Optional<BlockPos> sleepingPos = serverPlayer.getSleepingPos();
            if (sleepingPos.isPresent()) {
                if (sleepPositions.size() > 20)
                    sleepPositions.remove(0);

                sleepPositions.add(sleepingPos.get());

                PlayerSleepInfo sleepInfo = new PlayerSleepInfo(sleepPositions);

                Chalk.CriteriaTriggers.CONSECUTIVE_SLEEPING.trigger(serverPlayer, sleepInfo);

                String serializedDataStr = sleepInfo.serialize();
                serverPlayer.addTag("PlayerSleepInfo" + serializedDataStr);
            }
        }
    }
}
