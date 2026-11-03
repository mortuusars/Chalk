package io.github.mortuusars.chalk.neoforge.datagen;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.neoforge.datagen.generation.Advancements;
import io.github.mortuusars.chalk.neoforge.datagen.generation.LootTables;
import io.github.mortuusars.chalk.neoforge.datagen.generation.Recipes;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;

public final class BuiltInDatapackEntries {
    private BuiltInDatapackEntries() {
    }

    public static void register(GatherDataRegistryEntriesEvent event) {
        event.add(Chalk.Registries.MARK_SYMBOL, MarkSymbol::bootstrap);
        event.add(Registries.LOOT_TABLE, new LootTableProvider(Set.of(), List.of(
              new LootTableProvider.SubProviderEntry(LootTables.ChestLootTables::new, LootContextParamSets.CHEST))));
        event.add(new MultiRegistryBootstrap() {
            @Override
            public @NonNull Set<net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>>> requestedRegistries() {
                return Set.of(Registries.RECIPE, Registries.ADVANCEMENT);
            }

            @Override
            public void run(@NonNull BootstrapGetter contexts) {
                new Advancements(contexts.get(Registries.ADVANCEMENT)).generate();
                new Recipes(contexts.get(Registries.RECIPE), contexts.get(Registries.ADVANCEMENT)).generate();
            }
        });
    }
}
