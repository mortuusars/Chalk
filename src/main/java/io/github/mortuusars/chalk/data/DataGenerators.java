package io.github.mortuusars.chalk.data;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Chalk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    private DataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent gatherDataEvent){
        DataGenerator dataGenerator = gatherDataEvent.getGenerator();
        ExistingFileHelper existingFileHelper = gatherDataEvent.getExistingFileHelper();

        dataGenerator.addProvider(new ModRecipeProvider(dataGenerator));
        dataGenerator.addProvider(new ModLootTableProvider(dataGenerator));

        dataGenerator.addProvider(new BlockStateGenerator(dataGenerator, existingFileHelper));
        dataGenerator.addProvider(new ItemModelGenerator(dataGenerator, existingFileHelper));
    }
}
