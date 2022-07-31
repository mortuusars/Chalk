package io.github.mortuusars.chalk.data;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Chalk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent gatherDataEvent) {
        DataGenerator dataGenerator = gatherDataEvent.getGenerator();
        ExistingFileHelper existingFileHelper = gatherDataEvent.getExistingFileHelper();

        // Server
        dataGenerator.addProvider(gatherDataEvent.includeServer(), new ModRecipeProvider(dataGenerator));
        ModBlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(dataGenerator, existingFileHelper);
        dataGenerator.addProvider(gatherDataEvent.includeServer(), new ModItemTagsProvider(dataGenerator, blockTagsProvider, existingFileHelper));
        dataGenerator.addProvider(gatherDataEvent.includeServer(), blockTagsProvider);

        // Client
        dataGenerator.addProvider(gatherDataEvent.includeClient(), new BlockStateGenerator(dataGenerator, existingFileHelper));
        dataGenerator.addProvider(gatherDataEvent.includeClient(), new ItemModelGenerator(dataGenerator, existingFileHelper));
    }
}
