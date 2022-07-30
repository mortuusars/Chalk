package io.github.mortuusars.chalk.loot;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.CommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LootTableModification {
    @SubscribeEvent
    public static void LootTablesLoad(final LootTableLoadEvent event) {

        if (CommonConfig.GENERATE_IN_CHESTS.get()) {
            ResourceLocation tableId = event.getTable().getLootTableId();

            if (tableId.equals(BuiltInLootTables.ABANDONED_MINESHAFT) ||
                    tableId.equals(BuiltInLootTables.SIMPLE_DUNGEON)) {
                Chalk.LOGGER.debug("Adding Chalks to loot_table: " + event.getName().toString() + "...");
                event.getTable()
                        .addPool(LootPool.lootPool()
                                .add(LootTableReference.lootTableReference(new ResourceLocation(Chalk.MOD_ID, "chests/dungeon_chalk_loot")))
                                .build());
            } else if (tableId.equals(BuiltInLootTables.VILLAGE_CARTOGRAPHER) ||
                    tableId.equals(BuiltInLootTables.VILLAGE_MASON) ||
                    tableId.equals(BuiltInLootTables.VILLAGE_PLAINS_HOUSE) ||
                    tableId.equals(BuiltInLootTables.VILLAGE_SAVANNA_HOUSE) ||
                    tableId.equals(BuiltInLootTables.SPAWN_BONUS_CHEST)) {
                Chalk.LOGGER.debug("Adding Chalks to loot_table: " + event.getName().toString() + "...");
                event.getTable()
                        .addPool(LootPool.lootPool()
                                .add(LootTableReference.lootTableReference(new ResourceLocation(Chalk.MOD_ID, "chests/village_chalk_loot")))
                                .build());
            } else if (tableId.equals(BuiltInLootTables.DESERT_PYRAMID)) {
                Chalk.LOGGER.debug("Adding Chalks to loot_table: " + event.getName().toString() + "...");
                event.getTable()
                        .addPool(LootPool.lootPool()
                                .add(LootTableReference.lootTableReference(new ResourceLocation(Chalk.MOD_ID, "chests/desert_pyramid_chalk_loot")))
                                .build());
            }
        }

//        if (CommonConfig.GENERATE_CHALK_BOX_IN_CHESTS.get()) {
//            // TODO: create loot-tables with chalk box
//            // https://www.reddit.com/r/MinecraftCommands/comments/bzlt7v/loot_table_set_nbt_data/
//        }
    }
}
