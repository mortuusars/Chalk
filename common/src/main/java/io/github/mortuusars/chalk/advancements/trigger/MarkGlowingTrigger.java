package io.github.mortuusars.chalk.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.advancements.predicate.MarkPredicate;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.mortaar.advancement.predicate.MapColorPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MarkGlowingTrigger extends SimpleCriterionTrigger<MarkGlowingTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Mark mark, BlockPos markPos, MapColor surfaceColor) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, mark, markPos, surfaceColor));
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player,
                                  Optional<MarkPredicate> mark,
                                  Optional<LocationPredicate> location,
                                  Optional<MapColorPredicate> surfaceColor) implements SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
              instance.group(LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                          MarkPredicate.CODEC.optionalFieldOf("mark").forGetter(TriggerInstance::mark),
                          LocationPredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location),
                          MapColorPredicate.CODEC.optionalFieldOf("surface_color").forGetter(TriggerInstance::surfaceColor))
                    .apply(instance, TriggerInstance::new));

        public boolean matches(ServerPlayer player, Mark mark, BlockPos markPos, MapColor surfaceColor) {
            return (this.mark.isEmpty() || this.mark.get().matches(mark))
                  && (this.location.isEmpty() || this.location.get().matches(player.level(), markPos.getX() + 0.5, markPos.getY() + 0.5, markPos.getZ() + 0.5))
                  && (this.surfaceColor.isEmpty() || this.surfaceColor.get().matches(surfaceColor));
        }
    }
}
