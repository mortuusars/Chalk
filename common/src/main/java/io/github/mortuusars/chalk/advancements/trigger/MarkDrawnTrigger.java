package io.github.mortuusars.chalk.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.advancements.predicate.MarkPredicate;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.mortaar.advancement.predicate.MapColorPredicate;
import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MarkDrawnTrigger extends SimpleCriterionTrigger<MarkDrawnTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack drawingStack, BlockPos markPos, Mark mark, BlockPos surfacePos) {
        this.trigger(player, triggerInstance -> triggerInstance.matches(player, drawingStack, markPos, mark, surfacePos));
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player,
                                  Optional<ItemPredicate> item,
                                  Optional<LocationPredicate> location,
                                  Optional<MarkPredicate> mark,
                                  Optional<BlockPredicate> surfaceBlock,
                                  Optional<MapColorPredicate> surfaceColor) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
              instance.group(
                          LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                          ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item),
                          LocationPredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location),
                          MarkPredicate.CODEC.optionalFieldOf("mark").forGetter(TriggerInstance::mark),
                          BlockPredicate.CODEC.optionalFieldOf("surface").forGetter(TriggerInstance::surfaceBlock),
                          MapColorPredicate.CODEC.optionalFieldOf("surface_color").forGetter(TriggerInstance::surfaceColor))
                    .apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> structure(Holder<Structure> structureKey) {
            TriggerInstance instance = new TriggerInstance(EntityPredicate.wrap(
                  Optional.empty()),
                  Optional.empty(),
                  Optional.of(LocationPredicate.Builder.inStructure(structureKey).build()),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty());
            return Chalk.CriteriaTriggers.MARK_DRAWN.get().createCriterion(instance);
        }

        public static Criterion<MarkDrawnTrigger.TriggerInstance> structures(HolderSet<Structure> structures) {
            TriggerInstance instance = new TriggerInstance(EntityPredicate.wrap(
                  Optional.empty()),
                  Optional.empty(),
                  Optional.of(new LocationPredicate(Optional.empty(),
                        Optional.empty(),
                        Optional.of(structures),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty())),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty());
            return Chalk.CriteriaTriggers.MARK_DRAWN.get().createCriterion(instance);
        }

        public boolean matches(ServerPlayer player, ItemStack item, BlockPos markPos, Mark mark, BlockPos surfacePos) {
            return (this.item.isEmpty() || this.item.get().test(item))
                  && (this.location.isEmpty() || this.location.get().matches(player.level(), markPos.getX(), markPos.getY(), markPos.getZ()))
                  && (this.mark.isEmpty() || this.mark.get().matches(mark))
                  && (this.surfaceBlock.isEmpty() || this.surfaceBlock.get().matches(player.level(), surfacePos))
                  && (this.surfaceColor.isEmpty() || this.surfaceColor.get().matches(
                        player.level().getBlockState(surfacePos).getMapColor(player.level(), surfacePos)));
        }
    }

    public static class Builder {
        private @Nullable Holder<LootItemCondition> player;
        private @Nullable ItemPredicate item;
        private @Nullable LocationPredicate location;
        private @Nullable MarkPredicate mark;
        private @Nullable BlockPredicate surfaceBlock;
        private @Nullable MapColorPredicate surfaceColor;

        public Builder player(@Nullable Holder<LootItemCondition> player) {
            this.player = player;
            return this;
        }

        public Builder item(@Nullable ItemPredicate item) {
            this.item = item;
            return this;
        }

        public Builder location(@Nullable LocationPredicate location) {
            this.location = location;
            return this;
        }

        public Builder mark(@Nullable MarkPredicate mark) {
            this.mark = mark;
            return this;
        }

        public Builder surfaceBlock(@Nullable BlockPredicate surfaceBlock) {
            this.surfaceBlock = surfaceBlock;
            return this;
        }

        public Builder surfaceColor(@Nullable MapColorPredicate surfaceColor) {
            this.surfaceColor = surfaceColor;
            return this;
        }

        public MarkDrawnTrigger.TriggerInstance build() {
            return new TriggerInstance(
                  Optional.ofNullable(player),
                  Optional.ofNullable(item),
                  Optional.ofNullable(location),
                  Optional.ofNullable(mark),
                  Optional.ofNullable(surfaceBlock),
                  Optional.ofNullable(surfaceColor)
            );
        }
    }
}

