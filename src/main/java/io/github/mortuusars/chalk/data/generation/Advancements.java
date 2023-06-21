package io.github.mortuusars.chalk.data.generation;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.advancement.ChalkDrawTrigger;
import io.github.mortuusars.chalk.advancement.ConsecutiveSleepingTrigger;
import io.github.mortuusars.chalk.advancement.DyeColorPredicate;
import io.github.mortuusars.chalk.advancement.MapColorPredicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Advancements extends ForgeAdvancementProvider {
    public static final Logger LOGGER = LogManager.getLogger();

    public Advancements(DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), provider, existingFileHelper, List.of(new ChalkAdvancements()));
    }

    public static class ChalkAdvancements implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<Advancement> consumer, ExistingFileHelper existingFileHelper) {
            Advancement.Builder.advancement()
                    .parent(new ResourceLocation("minecraft:adventure/kill_a_mob"))
                    .display(Items.SKELETON_SKULL,
                            Component.translatable("advancement.chalk.get_skeleton_skull"),
                            Component.translatable("advancement.chalk.get_skeleton_skull.description"),
                            null, FrameType.TASK, true, true, false)
                    .addCriterion("slept_in_bed", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SKELETON_SKULL))
                    .save(consumer, Chalk.resource("adventure/get_skeleton_skull"), existingFileHelper);

            Advancement.Builder.advancement()
                    .parent(new ResourceLocation("minecraft:adventure/sleep_in_bed"))
                    .display(Items.YELLOW_BED,
                            Component.translatable("advancement.chalk.sleep_three_times_in_one_place"),
                            Component.translatable("advancement.chalk.sleep_three_times_in_one_place.description"),
                            null, FrameType.TASK, true, true, false)
                    .addCriterion("sleep_three_times_in_one_place", new ConsecutiveSleepingTrigger.TriggerInstance(ContextAwarePredicate.ANY,
                            MinMaxBounds.Ints.atLeast(3), DistancePredicate.absolute(MinMaxBounds.Doubles.atMost(16))))
                    .save(consumer, Chalk.resource("adventure/sleep_three_times_in_one_place"), existingFileHelper);


            Advancement drawInStructure = Advancement.Builder.advancement()
                    .parent(new ResourceLocation("minecraft:adventure/root"))
                    .display(Chalk.Items.getChalk(DyeColor.YELLOW),
                            Component.translatable("advancement.chalk.draw_mark_in_maze"),
                            Component.translatable("advancement.chalk.draw_mark_in_maze.description"),
                            null, FrameType.TASK, true, true, false)
                    .requirements(RequirementsStrategy.OR)
                    .addCriterion("draw_in_mineshaft", ChalkDrawTrigger.TriggerInstance.structure(BuiltinStructures.MINESHAFT))
                    .addCriterion("draw_in_mineshaft_mesa", ChalkDrawTrigger.TriggerInstance.structure(BuiltinStructures.MINESHAFT_MESA))
                    .addCriterion("draw_in_fortress", ChalkDrawTrigger.TriggerInstance.structure(BuiltinStructures.FORTRESS))
                    .addCriterion("draw_in_stronghold", ChalkDrawTrigger.TriggerInstance.structure(BuiltinStructures.STRONGHOLD))
                    .save(consumer, Chalk.resource("adventure/draw_mark_in_maze"), existingFileHelper);

            Advancement.Builder.advancement()
                    .parent(drawInStructure)
                    .display(Chalk.Items.getChalk(DyeColor.LIGHT_GRAY),
                            Component.translatable("advancement.chalk.vandalism"),
                            Component.translatable("advancement.chalk.vandalism.description"),
                            null, FrameType.TASK, true, true, true)
                    .requirements(RequirementsStrategy.OR)
                    .addCriterion("draw_in_village_plains", ChalkDrawTrigger.TriggerInstance.structure(BuiltinStructures.VILLAGE_PLAINS))
                    .addCriterion("draw_in_village_desert", ChalkDrawTrigger.TriggerInstance.structure(BuiltinStructures.VILLAGE_DESERT))
                    .addCriterion("draw_in_village_savanna", ChalkDrawTrigger.TriggerInstance.structure(BuiltinStructures.VILLAGE_SAVANNA))
                    .addCriterion("draw_in_village_snowy", ChalkDrawTrigger.TriggerInstance.structure(BuiltinStructures.VILLAGE_SNOWY))
                    .addCriterion("draw_in_village_taiga", ChalkDrawTrigger.TriggerInstance.structure(BuiltinStructures.VILLAGE_TAIGA))
                    .rewards(AdvancementRewards.Builder.experience(100))
                    .save(consumer, Chalk.resource("adventure/vandalism"), existingFileHelper);

            Advancement.Builder.advancement()
                    .parent(drawInStructure)
                    .display(Chalk.Items.getChalk(DyeColor.BLACK),
                            Component.translatable("advancement.chalk.black_chalk_on_black_block"),
                            Component.translatable("advancement.chalk.black_chalk_on_black_block.description"),
                            null, FrameType.TASK, true, true, true)
                    .addCriterion("draw_with_chalk_color", new ChalkDrawTrigger.TriggerInstance(ContextAwarePredicate.ANY,
                            LocationPredicate.Builder.location()
                                    .setLight(new LightPredicate.Builder()
                                            .setComposite(MinMaxBounds.Ints.atMost(7))
                                            .build())
                                    .build(),
                            new MapColorPredicate(List.of(MapColor.COLOR_BLACK)), new DyeColorPredicate(List.of(DyeColor.BLACK))))
                    .rewards(AdvancementRewards.Builder.experience(100))
                    .save(consumer, Chalk.resource("adventure/black_chalk_on_black_block"), existingFileHelper);
        }
    }
}