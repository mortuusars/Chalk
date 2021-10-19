package io.github.mortuusars.chalk.loot;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.CommonConfig;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LootTableModification {
    @SubscribeEvent
    public static void LootTablesLoad(final LootTableLoadEvent event){

        if (!CommonConfig.GENERATE_IN_CHESTS.get())
            return;

        ResourceLocation tableId = event.getTable().getLootTableId();

        if (tableId.equals( LootTables.ABANDONED_MINESHAFT) ||
                tableId.equals(LootTables.SIMPLE_DUNGEON)) {
            Chalk.LOGGER.info("Adding Chalks to loot_table: " + event.getName().toString() + "...");
            event.getTable()
                    .addPool(LootPool.lootPool()
                            .add(TableLootEntry.lootTableReference(new ResourceLocation(Chalk.MOD_ID, "chests/dungeon_chalk_loot")))
                            .build());
        } else if (tableId.equals(LootTables.VILLAGE_CARTOGRAPHER) ||
                tableId.equals(LootTables.VILLAGE_MASON) ||
                tableId.equals(LootTables.VILLAGE_PLAINS_HOUSE) ||
                tableId.equals(LootTables.VILLAGE_SAVANNA_HOUSE) ||
                tableId.equals(LootTables.SPAWN_BONUS_CHEST)) {
            Chalk.LOGGER.info("Adding Chalks to loot_table: " + event.getName().toString() + "...");
            event.getTable()
                    .addPool(LootPool.lootPool()
                            .add(TableLootEntry.lootTableReference(new ResourceLocation(Chalk.MOD_ID, "chests/village_chalk_loot")))
                            .build());
        } else if (tableId.equals(LootTables.DESERT_PYRAMID)) {
            Chalk.LOGGER.info("Adding Chalks to loot_table: " + event.getName().toString() + "...");
            event.getTable()
                    .addPool(LootPool.lootPool()
                            .add(TableLootEntry.lootTableReference(new ResourceLocation(Chalk.MOD_ID, "chests/desert_pyramid_chalk_loot")))
                            .build());
        }
    }
}
