package io.github.mortuusars.chalk.loot;

import io.github.mortuusars.chalk.Chalk;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LootTableModification {
    @SubscribeEvent
    public static void LootTablesLoad(final LootTableLoadEvent event){
        if (event.getName().toString().equals("minecraft:chests/abandoned_mineshaft") ||
            event.getName().toString().equals("minecraft:chests/simple_dungeon")){

            Chalk.LOGGER.debug("Adding Chalks to loot_table: " + event.getName().toString());

            event.getTable().addPool(LootPool.lootPool().add(TableLootEntry.lootTableReference(
                    new ResourceLocation(Chalk.MOD_ID, "chests/dungeon_chalk_loot"))).build());
        }
        else if (event.getName().toString().equals("minecraft:chests/village/village_cartographer") ||
                event.getName().toString().equals("minecraft:chests/village/village_savanna_house") ||
                event.getName().toString().equals("minecraft:chests/village/village_plains_house")) {

            Chalk.LOGGER.debug("Adding Chalks to loot_table: " + event.getName().toString());

            event.getTable().addPool(LootPool.lootPool().add(TableLootEntry.lootTableReference(
                    new ResourceLocation(Chalk.MOD_ID, "chests/village_chalk_loot"))).build());
        }
    }
}
