package io.github.mortuusars.chalk.advancement;

import com.google.gson.JsonObject;
import io.github.mortuusars.chalk.Chalk;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConsecutiveSleepingTrigger extends SimpleCriterionTrigger<ConsecutiveSleepingTrigger.TriggerInstance> {
    private static final ResourceLocation ID = Chalk.resource("consecutive_sleeping_pos");

    @Override
    protected @NotNull TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, @NotNull DeserializationContext conditionsParser) {
        return new TriggerInstance(predicate, MinMaxBounds.Ints.fromJson(json.get("count")), DistancePredicate.fromJson(json.get("distance")));
    }

    public void trigger(ServerPlayer player, PlayerSleepInfo sleepInfo) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, sleepInfo));
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints count;
        private final DistancePredicate distance;

        public TriggerInstance(ContextAwarePredicate predicate, MinMaxBounds.Ints count, DistancePredicate distance) {
            super(ConsecutiveSleepingTrigger.ID, predicate);
            this.count = count;
            this.distance = distance;
        }

        @Override
        public @NotNull JsonObject serializeToJson(@NotNull SerializationContext conditions) {
            JsonObject jsonObject = super.serializeToJson(conditions);
            jsonObject.add("count", count.serializeToJson());
            jsonObject.add("distance", distance.serializeToJson());
            return jsonObject;
        }

        @SuppressWarnings("unused")
        public boolean matches(ServerPlayer player, PlayerSleepInfo sleepInfo) {
            List<BlockPos> sleepPositions = sleepInfo.sleepPositions();

            if (sleepPositions.isEmpty())
                return false;
            else if (sleepPositions.size() == 1)
                return count.matches(1);

            BlockPos lastSleepPos = sleepPositions.get(sleepPositions.size() - 1);

            int matchedDistanceCount = 1;

            for (int i = sleepPositions.size() - 2; i >= 0 ; i--) {
                BlockPos pos = sleepPositions.get(i);

                if (distance.matches(lastSleepPos.getX(), lastSleepPos.getY(), lastSleepPos.getZ(),
                        pos.getX(), pos.getY(), pos.getZ()))
                    matchedDistanceCount++;
                else
                    break;
            }

            return count.matches(matchedDistanceCount);
        }
    }
}
