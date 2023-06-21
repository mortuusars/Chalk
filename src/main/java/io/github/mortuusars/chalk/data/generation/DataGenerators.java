package io.github.mortuusars.chalk.data.generation;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Chalk.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent gatherDataEvent) {
        DataGenerator dataGenerator = gatherDataEvent.getGenerator();
        ExistingFileHelper existingFileHelper = gatherDataEvent.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = gatherDataEvent.getLookupProvider();

        // Server
        dataGenerator.addProvider(gatherDataEvent.includeServer(), new Recipes(dataGenerator));
        ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(dataGenerator, lookupProvider, existingFileHelper);
        dataGenerator.addProvider(gatherDataEvent.includeServer(), new ModItemTagsProvider(dataGenerator, lookupProvider, blockTagsProvider, existingFileHelper));
        dataGenerator.addProvider(gatherDataEvent.includeServer(), blockTagsProvider);
        dataGenerator.addProvider(gatherDataEvent.includeServer(), new Advancements(dataGenerator, lookupProvider, existingFileHelper));

        // Client
        dataGenerator.addProvider(gatherDataEvent.includeClient(), new BlockStateGenerator(dataGenerator, existingFileHelper));
        dataGenerator.addProvider(gatherDataEvent.includeClient(), new ItemModelGenerator(dataGenerator, existingFileHelper));
    }
}
