package io.github.mortuusars.chalk.loot;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.event.LootTableLoadEvent;

public class LootTableModification {
    public static void LootTablesLoad(final LootTableLoadEvent event) {
        if (!Config.GENERATE_IN_CHESTS.get())
            return;

        ResourceLocation tableId = event.getTable().getLootTableId();

        if (tableId.equals(BuiltInLootTables.ABANDONED_MINESHAFT) ||
                tableId.equals(BuiltInLootTables.SIMPLE_DUNGEON)) {
            event.getTable()
                    .addPool(LootPool.lootPool()
                            .add(LootTableReference.lootTableReference(Chalk.resource("chests/dungeon_chalk_loot")))
                            .build());
        }

        if (tableId.equals(BuiltInLootTables.VILLAGE_CARTOGRAPHER) ||
                tableId.equals(BuiltInLootTables.VILLAGE_MASON) ||
                tableId.equals(BuiltInLootTables.VILLAGE_PLAINS_HOUSE) ||
                tableId.equals(BuiltInLootTables.VILLAGE_SAVANNA_HOUSE) ||
                tableId.equals(BuiltInLootTables.SPAWN_BONUS_CHEST)) {
            event.getTable()
                    .addPool(LootPool.lootPool()
                            .add(LootTableReference.lootTableReference(Chalk.resource("chests/village_chalk_loot")))
                            .build());
        }

        if (tableId.equals(BuiltInLootTables.DESERT_PYRAMID)) {
            event.getTable()
                    .addPool(LootPool.lootPool()
                            .add(LootTableReference.lootTableReference(Chalk.resource("chests/desert_pyramid_chalk_loot")))
                            .build());
        }
    }
}
