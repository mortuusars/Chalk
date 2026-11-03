package io.github.mortuusars.chalk.event;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.advancements.PlayerSleepInfo;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.mortaar.util.supporter.Supporters;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommonEvents {
    public static void commonSetup() {

    }

    public static void onAdvancementAward(ServerPlayer player, AdvancementHolder advancement) {
        if (!Config.Server.SYMBOL_UNLOCKING_CHAT_MESSAGE.get()) {
            return;
        }

        List<Holder<MarkSymbol>> unlockedSymbols = MarkSymbol.getAllHolders(player.registryAccess(), Supporters.isEligibleForGoldenRewards(player.getUUID()))
              .filter(symbol -> symbol.value().requiredAdvancement()
                    .map(id -> id.equals(advancement.id())).orElse(false))
              .toList();

        if (!unlockedSymbols.isEmpty()) {
            if (unlockedSymbols.size() == 1) {
                unlockedSymbols.getFirst().unwrapKey().ifPresent(key ->
                      player.sendSystemMessage(Component.translatable("chat.chalk.symbol_unlocked", Component.translatable(
                            key.identifier().toLanguageKey("mark_symbol")).withStyle(Style.EMPTY.withColor(0x53a5df))), false));
            } else {
                List<MutableComponent> symbolNames = unlockedSymbols.stream()
                      .filter(s -> s.unwrapKey().isPresent())
                      .map(s -> Component.translatable(s.unwrapKey().orElseThrow().identifier().toLanguageKey("mark_symbol"))
                            .withStyle(Style.EMPTY.withColor(0x53a5df)))
                      .toList();

                MutableComponent symbolsListComponent = Component.empty();

                for (int i = 0; i < symbolNames.size(); i++) {
                    if (i != 0) {
                        symbolsListComponent.append(Component.literal(", "));
                    }
                    symbolsListComponent.append(symbolNames.get(i));
                }

                player.sendSystemMessage(Component.translatable("chat.chalk.symbol_unlocked", symbolsListComponent), false);
            }

            player.level().playSound(null, player, Chalk.SoundEvents.MARK_DRAWN.get(), SoundSource.PLAYERS, 1f, 1f);
        }
    }

    public static void onStoppedSleeping(ServerPlayer player) {
        boolean sleepingLongEnough = player.isSleepingLongEnough();
        if (!sleepingLongEnough) {
            return;
        }

        List<BlockPos> sleepPositions = new ArrayList<>();

        @Nullable String tag = null;
        for (String t : player.entityTags()) {
            if (t.startsWith("ChalkConsecutiveSleepPositions")) {
                tag = t;
                break;
            }
        }

        if (tag != null) {
            player.removeTag(tag);
            String dataStr = tag.replace("ChalkConsecutiveSleepPositions", "");
            PlayerSleepInfo sleepInfo = PlayerSleepInfo.deserialize(dataStr);
            sleepPositions = new ArrayList<>(sleepInfo.sleepPositions());
        }

        Optional<BlockPos> sleepingPos = player.getSleepingPos();
        if (sleepingPos.isPresent()) {
            if (sleepPositions.size() > 20) {
                sleepPositions.removeFirst();
            }

            sleepPositions.add(sleepingPos.get());

            PlayerSleepInfo sleepInfo = new PlayerSleepInfo(sleepPositions);

            Chalk.CriteriaTriggers.CONSECUTIVE_SLEEPING.get().trigger(player, sleepInfo);

            String serializedDataStr = sleepInfo.serialize();
            player.addTag("ChalkConsecutiveSleepPositions" + serializedDataStr);
        }
    }
}
