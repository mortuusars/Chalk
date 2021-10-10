package io.github.mortuusars.chalk.data;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.chalk.setup.ModBlocks;
import io.github.mortuusars.chalk.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return ImmutableList.of(
                Pair.of(ModBlockLootTables::new, LootParameterSets.BLOCK)
        );
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
        map.forEach((resLocation, table) -> LootTableManager.validate(validationtracker, resLocation, table));
    }

    public static class ModBlockLootTables extends BlockLootTables {

        @Override
        protected void addTables() {
            this.add(ModBlocks.WHITE_CHALK_MARK_BLOCK.get(), noDrop());
            this.add(ModBlocks.RED_CHALK_MARK_BLOCK.get(), noDrop());

//            this.add(ModBlocks.WHITE_PLACED_CHALK_BLOCK.get(), createSingleItemTable(ModItems.WHITE_CHALK.get()));
//            this.add(ModBlocks.RED_PLACED_CHALK_BLOCK.get(), createSingleItemTable(ModItems.RED_CHALK.get()));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return Registry.BLOCKS.getEntries().stream().map(RegistryObject::get).collect(Collectors.toList());
        }
    }

}
