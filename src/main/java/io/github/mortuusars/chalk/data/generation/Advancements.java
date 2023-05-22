package io.github.mortuusars.chalk.data.generation;

import com.google.common.collect.Sets;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.advancement.ConsecutiveSleepingPositionTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

public class Advancements extends AdvancementProvider {
    private final Path PATH;
    private final ExistingFileHelper existingFileHelper;
    public static final Logger LOGGER = LogManager.getLogger();

    public Advancements(DataGenerator dataGenerator, ExistingFileHelper existingFileHelper) {
        super(dataGenerator, existingFileHelper);
        PATH = dataGenerator.getOutputFolder();
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public void run(@NotNull CachedOutput cache) {
        Consumer<Advancement> consumer = getOutput(cache);

        CompoundTag almostExpiredTag = new CompoundTag();
        almostExpiredTag.putBoolean("almostExpired", true);

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
                .addCriterion("sleep_three_times_in_one_place", new ConsecutiveSleepingPositionTrigger.TriggerInstance(EntityPredicate.Composite.ANY,
                        MinMaxBounds.Ints.atLeast(3), DistancePredicate.absolute(MinMaxBounds.Doubles.atMost(16))))
                .save(consumer, Chalk.resource("adventure/sleep_three_times_in_one_place"), existingFileHelper);
    }

    protected Consumer<Advancement> getOutput(CachedOutput cache) {
        Set<ResourceLocation> set = Sets.newHashSet();
        return (advancement) -> {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            } else {
                Path path1 = PATH.resolve("data/" + advancement.getId().getNamespace() + "/advancements/" + advancement.getId().getPath() + ".json");

                try {
                    DataProvider.saveStable(cache, advancement.deconstruct().serializeToJson(), path1);
                }
                catch (IOException ioexception) {
                    LOGGER.error("Couldn't save advancement {}", path1, ioexception);
                }
            }
        };
    }
}