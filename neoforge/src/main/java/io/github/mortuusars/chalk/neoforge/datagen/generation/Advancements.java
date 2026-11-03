package io.github.mortuusars.chalk.neoforge.datagen.generation;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.advancements.predicate.MarkPredicate;
import io.github.mortuusars.chalk.advancements.trigger.MarkDrawnTrigger;
import io.github.mortuusars.chalk.advancements.trigger.ConsecutiveSleepingTrigger;
import io.github.mortuusars.chalk.advancements.trigger.MarkGlowingTrigger;
import io.github.mortuusars.mortaar.advancement.predicate.ColorPredicate;
import io.github.mortuusars.mortaar.advancement.predicate.MapColorPredicate;
import net.minecraft.advancements.*;
import net.minecraft.advancements.predicates.*;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.material.MapColor;
import java.util.List;
import java.util.Optional;

public class Advancements extends AdvancementSubProvider {
    public Advancements(BootstrapContext<Advancement> output) {
        super(output);
    }

    @Override
    public void generate() {
        Advancement.Builder.advancement()
              .parent(Identifier.parse("minecraft:adventure/sleep_in_bed"))
              .display(Items.BED.pick(DyeColor.YELLOW),
                    Component.translatable("advancement.chalk.home_is_where_the_bed_is.title"),
                    Component.translatable("advancement.chalk.home_is_where_the_bed_is.description"),
                    AdvancementType.TASK, true, true, false)
              .addCriterion("sleep_three_times_in_one_place", Chalk.CriteriaTriggers.CONSECUTIVE_SLEEPING.get().createCriterion(
                    new ConsecutiveSleepingTrigger.TriggerInstance(EntityPredicate.wrap(Optional.empty()),
                          Optional.empty(),
                          MinMaxBounds.Ints.atLeast(3),
                          DistancePredicate.absolute(MinMaxBounds.Doubles.atMost(16)))))
              .save(output, Chalk.resource("adventure/home_is_where_the_bed_is").toString());

        AdvancementHolder thisWay = Advancement.Builder.advancement()
              .parent(Identifier.parse("minecraft:adventure/root"))
              .display(chalkTemplate(0xFFDB4A, 0),
                    Component.translatable("advancement.chalk.this_way.title"),
                    Component.translatable("advancement.chalk.this_way.description"),
                    AdvancementType.TASK, true, true, false)
              .requirements(AdvancementRequirements.Strategy.OR)
              .addCriterion("draw_in_mineshaft", MarkDrawnTrigger.TriggerInstance.structure(output.lookup(Registries.STRUCTURE).getOrThrow(BuiltinStructures.MINESHAFT)))
              .addCriterion("draw_in_mineshaft_mesa", MarkDrawnTrigger.TriggerInstance.structure(output.lookup(Registries.STRUCTURE).getOrThrow(BuiltinStructures.MINESHAFT_MESA)))
              .addCriterion("draw_in_fortress", MarkDrawnTrigger.TriggerInstance.structure(output.lookup(Registries.STRUCTURE).getOrThrow(BuiltinStructures.FORTRESS)))
              .addCriterion("draw_in_stronghold", MarkDrawnTrigger.TriggerInstance.structure(output.lookup(Registries.STRUCTURE).getOrThrow(BuiltinStructures.STRONGHOLD)))
              .addCriterion("draw_in_trial_chambers", MarkDrawnTrigger.TriggerInstance.structure(output.lookup(Registries.STRUCTURE).getOrThrow(BuiltinStructures.TRIAL_CHAMBERS)))
              .save(output, Chalk.resource("adventure/this_way").toString());

        AdvancementHolder vandalism = Advancement.Builder.advancement()
              .parent(thisWay)
              .display(chalkTemplate(0xBDBFBE, 0),
                    Component.translatable("advancement.chalk.vandalism.title"),
                    Component.translatable("advancement.chalk.vandalism.description"),
                    AdvancementType.TASK, true, true, true)
              .requirements(AdvancementRequirements.Strategy.OR)
              .addCriterion("draw_in_village", MarkDrawnTrigger.TriggerInstance.structures(output.lookup(Registries.STRUCTURE).get(StructureTags.VILLAGE).get()))
              .rewards(AdvancementRewards.Builder.experience(50))
              .save(output, Chalk.resource("adventure/vandalism").toString());

        AdvancementHolder guidingStar = Advancement.Builder.advancement()
              .parent(thisWay)
              .display(Items.GLOWSTONE_DUST,
                    Component.translatable("advancement.chalk.guiding_star.title"),
                    Component.translatable("advancement.chalk.guiding_star.description"),
                    AdvancementType.TASK, true, true, false)
              .addCriterion("make_glowing", Chalk.CriteriaTriggers.MARK_GLOWING.get().createCriterion(
                    new MarkGlowingTrigger.TriggerInstance(EntityPredicate.wrap(Optional.empty()),
                    Optional.empty(),
                    Optional.of(LocationPredicate.Builder.location()
                          .setLight(new LightPredicate.Builder()
                                .setComposite(MinMaxBounds.Ints.atMost(7)))
                          .build()),
                    Optional.empty())))
              .save(output, Chalk.resource("adventure/guiding_star").toString());

        AdvancementHolder consumedByTheLight = Advancement.Builder.advancement()
              .parent(guidingStar)
              .display(chalkTemplate(0xFFFFFF, 0),
                    Component.translatable("advancement.chalk.consumed_by_the_light.title"),
                    Component.translatable("advancement.chalk.consumed_by_the_light.description"),
                    AdvancementType.TASK, true, true, false)
              .addCriterion("draw_white", Chalk.CriteriaTriggers.MARK_DRAWN.get().createCriterion(
                    new MarkDrawnTrigger.Builder()
                          .location(LocationPredicate.Builder.location()
                                .setLight(new LightPredicate.Builder().setComposite(MinMaxBounds.Ints.atLeast(11)))
                                .build())
                          .mark(new MarkPredicate.Builder()
                                .color(ColorPredicate.builder()
                                      .red(MinMaxBounds.Ints.atLeast(200))
                                      .green(MinMaxBounds.Ints.atLeast(200))
                                      .blue(MinMaxBounds.Ints.atLeast(200))
                                      .build())
                                .build())
                          .surfaceColor(new MapColorPredicate(List.of(MapColor.TERRACOTTA_WHITE, MapColor.SNOW, MapColor.QUARTZ)))
                          .build()))
              .save(output, Chalk.resource("adventure/consumed_by_the_light").toString());

        AdvancementHolder aloneInTheDarkness = Advancement.Builder.advancement()
              .parent(consumedByTheLight)
              .display(chalkTemplate(0x2C2D2E, 0),
                    Component.translatable("advancement.chalk.alone_in_the_darkness.title"),
                    Component.translatable("advancement.chalk.alone_in_the_darkness.description"),
                    AdvancementType.TASK, true, true, false)
              .addCriterion("draw_black", Chalk.CriteriaTriggers.MARK_DRAWN.get().createCriterion(
                    new MarkDrawnTrigger.Builder()
                          .location(LocationPredicate.Builder.location()
                                .setLight(new LightPredicate.Builder().setComposite(MinMaxBounds.Ints.atMost(7)))
                                .build())
                          .mark(new MarkPredicate.Builder()
                                .color(ColorPredicate.builder()
                                      .red(MinMaxBounds.Ints.atMost(50))
                                      .green(MinMaxBounds.Ints.atMost(50))
                                      .blue(MinMaxBounds.Ints.atMost(50))
                                      .build())
                                .build())
                          .surfaceColor(new MapColorPredicate(List.of(MapColor.COLOR_BLACK)))
                          .build()))
              .save(output, Chalk.resource("adventure/alone_in_the_darkness").toString());

        AdvancementHolder paintTheGrass = Advancement.Builder.advancement()
              .parent(aloneInTheDarkness)
              .display(chalkTemplate(0x78C73C, 0),
                    Component.translatable("advancement.chalk.paint_the_grass.title"),
                    Component.translatable("advancement.chalk.paint_the_grass.description"),
                    AdvancementType.TASK, true, true, false)
              .addCriterion("draw_green", Chalk.CriteriaTriggers.MARK_DRAWN.get().createCriterion(
                    new MarkDrawnTrigger.Builder()
                          .mark(new MarkPredicate.Builder()
                                .color(ColorPredicate.builder()
                                      .hue(MinMaxBounds.Doubles.between(0.2, 0.35))
                                      .saturation(MinMaxBounds.Doubles.atLeast(0.4))
                                      .brightness(MinMaxBounds.Doubles.atLeast(0.4))
                                      .build())
                                .build())
                          .surfaceBlock(BlockPredicate.Builder.block()
                                .of(output.lookup(Registries.BLOCK), BlockTags.DIRT)
                                .of(output.lookup(Registries.BLOCK), BlockTags.GRASS_BLOCKS)
                                .build())
                          .build()))
              .save(output, Chalk.resource("adventure/paint_the_grass").toString());
    }

    private static ItemStackTemplate chalkTemplate(int color, int damage) {
        DataComponentPatch.Builder components = DataComponentPatch.builder();
        if (color != 0xFFFFFF) {
            components.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        }
        if (damage > 0) {
            components.set(DataComponents.DAMAGE, damage);
        }
        return new ItemStackTemplate(Chalk.Items.CHALK.get(), 1, components.build());
    }
}
