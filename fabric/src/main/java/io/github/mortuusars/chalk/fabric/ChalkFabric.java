package io.github.mortuusars.chalk.fabric;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.world.chalk.ChalkColors;
import io.github.mortuusars.chalk.event.CommonEvents;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.neoforged.fml.config.ModConfig;

public class ChalkFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Chalk.init();

        ConfigRegistry.INSTANCE.register(Chalk.ID, ModConfig.Type.SERVER, Config.Server.SPEC);
        ConfigRegistry.INSTANCE.register(Chalk.ID, ModConfig.Type.COMMON, Config.Common.SPEC);
        ConfigRegistry.INSTANCE.register(Chalk.ID, ModConfig.Type.CLIENT, Config.Client.SPEC);

        ModConfigEvents.loading(Chalk.ID).register(config -> {
            if (config.getType() == ModConfig.Type.SERVER) {
                Config.Server.loading();
            }
        });
        ModConfigEvents.reloading(Chalk.ID).register(config -> {
            if (config.getType() == ModConfig.Type.SERVER) {
                Config.Server.reloading();
            }
        });

        CommonEvents.commonSetup();

        DynamicRegistries.registerSynced(Chalk.Registries.MARK_SYMBOL, MarkSymbol.DIRECT_CODEC, MarkSymbol.DIRECT_CODEC);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(event -> {
            event.accept(new ItemStack(Chalk.Items.CHALK.get()));

            if (Config.Server.ADD_DYED_CHALKS_TO_TAB.get()) {
                for (DyeColor dyeColor : ChalkColors.ORDERED_DYE_COLORS) {
                    if (dyeColor == DyeColor.WHITE) {
                        continue;
                    }

                    ItemStack stack = new ItemStack(Chalk.Items.CHALK.get());
                    int color = Chalk.Items.CHALK.get().getColorFromDye(stack, dyeColor);
                    stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
                    event.accept(stack);
                }
            }

            event.accept(Chalk.Items.CHALK_BOX.get());
        });

        LootTableEvents.MODIFY.register(ChalkFabric::modifyLoot);
    }

    private static void modifyLoot(ResourceKey<LootTable> tableKey, LootTable.Builder builder,
                                   LootTableSource source, HolderLookup.Provider registries) {
        if (!Config.Common.LOOT.get()) {
            return;
        }

        if (BuiltInLootTables.ABANDONED_MINESHAFT.equals(tableKey)) {
            registries.get(Chalk.LootTables.ABANDONED_MINESHAFT_CHALKS)
                        .ifPresent(table -> builder.pool(LootPool.lootPool()
                              .add(NestedLootTable.lootTableReference(table))
                              .build()));
        }
        if (BuiltInLootTables.DESERT_PYRAMID.equals(tableKey)) {
            registries.get(Chalk.LootTables.DESERT_PYRAMID_CHALKS)
                  .ifPresent(table -> builder.pool(LootPool.lootPool()
                        .add(NestedLootTable.lootTableReference(table))
                        .build()));
        }
        if (BuiltInLootTables.VILLAGE_CARTOGRAPHER.equals(tableKey)
              || BuiltInLootTables.VILLAGE_MASON.equals(tableKey)
              || BuiltInLootTables.VILLAGE_PLAINS_HOUSE.equals(tableKey)
              || BuiltInLootTables.VILLAGE_SAVANNA_HOUSE.equals(tableKey)) {
            registries.get(Chalk.LootTables.VILLAGE_CHALKS)
                  .ifPresent(table -> builder.pool(LootPool.lootPool()
                        .add(NestedLootTable.lootTableReference(table))
                        .build()));
        }
        if (BuiltInLootTables.SIMPLE_DUNGEON.equals(tableKey)) {
            registries.get(Chalk.LootTables.SIMPLE_DUNGEON_CHALKS)
                  .ifPresent(table -> builder.pool(LootPool.lootPool()
                        .add(NestedLootTable.lootTableReference(table))
                        .build()));
        }
        if (BuiltInLootTables.SPAWN_BONUS_CHEST.equals(tableKey)) {
            registries.get(Chalk.LootTables.VILLAGE_CHALKS)
                  .ifPresent(table -> builder.pool(LootPool.lootPool()
                        .add(NestedLootTable.lootTableReference(table))
                        .build()));
        }
    }
}
