package io.github.mortuusars.chalk.neoforge.datagen;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.neoforge.datagen.generation.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Chalk.ID)
public class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherDataServer(GatherDataEvent.Server event) {
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
    }

    @SubscribeEvent
    public static void gatherDataRegistries(GatherDataRegistryEntriesEvent event) {
        BuiltInDatapackEntries.register(event);
    }
}
