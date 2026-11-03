package io.github.mortuusars.chalk.neoforge.datagen.generation;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.item.ChalkItem;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.List;
import java.util.Optional;

public class LootTables {
    private static final Optional<ItemStackTemplate> EMPTY = Optional.empty();

    public static class ChestLootTables implements LootTableSubProvider {
        private final LootTableSubProvider.Context context;

        public ChestLootTables(LootTableSubProvider.Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            context.accept(Chalk.LootTables.ABANDONED_MINESHAFT_CHALKS,
                  LootTable.lootTable().withPool(
                        LootPool.lootPool()
                              .setRolls(ContextIntProviders.exactly(2))
                              .add(chalkBox(List.of(
                                    chalkStack(DyeColor.WHITE, 23), EMPTY, EMPTY,
                                    EMPTY, EMPTY, chalkStack(DyeColor.LIGHT_GRAY, 0),
                                    EMPTY, chalkStack(DyeColor.RED, 17), EMPTY), 0)
                                    .setWeight(2))
                              .add(chalkBox(List.of(
                                    EMPTY, chalkStack(DyeColor.LIGHT_GRAY, 0), EMPTY,
                                    EMPTY, chalkStack(DyeColor.WHITE, 0), EMPTY,
                                    EMPTY, chalkStack(DyeColor.LIGHT_BLUE, 6), EMPTY,
                                    Optional.of(new ItemStackTemplate(Items.GLOWSTONE_DUST, 7))), 3)
                                    .setWeight(2))
                              .add(chalkBox(List.of(
                                    chalkStack(DyeColor.BLUE, 5), EMPTY, chalkStack(DyeColor.PINK, 50),
                                    EMPTY, EMPTY, EMPTY,
                                    chalkStack(DyeColor.YELLOW, 0), EMPTY, EMPTY,
                                    Optional.of(new ItemStackTemplate(Items.GLOW_INK_SAC))), 5)
                                    .setWeight(2))
                              .add(chalkBox(List.of(
                                    EMPTY, chalkStack(DyeColor.LIGHT_GRAY, 0), EMPTY,
                                    EMPTY, EMPTY, EMPTY,
                                    chalkStack(DyeColor.GREEN, 14), EMPTY, EMPTY), 0)
                                    .setWeight(2))
                              .add(chalk(DyeColor.WHITE, 10))
                              .add(chalk(DyeColor.LIGHT_GRAY, 8))
                              .add(chalk(DyeColor.BLACK, 4))
                              .add(EmptyLootItem.emptyItem().setWeight(50))
                  ));

            context.accept(Chalk.LootTables.DESERT_PYRAMID_CHALKS,
                  LootTable.lootTable().withPool(
                        LootPool.lootPool()
                              .setRolls(ContextIntProviders.exactly(2))
                              .add(chalkBox(List.of(
                                    chalkStack(DyeColor.WHITE, 23), EMPTY, EMPTY, EMPTY,
                                    chalkStack(DyeColor.LIGHT_GRAY, 12), EMPTY, EMPTY, chalkStack(DyeColor.RED, 0),
                                    EMPTY), 0)
                                    .setWeight(2))
                              .add(chalkBox(List.of(
                                    EMPTY, chalkStack(DyeColor.LIGHT_GRAY, 5), EMPTY,
                                    chalkStack(DyeColor.WHITE, 0), EMPTY, EMPTY,
                                    chalkStack(DyeColor.LIGHT_BLUE, 15), EMPTY, EMPTY,
                                    Optional.of(new ItemStackTemplate(Items.GLOWSTONE_DUST, 4))), 4)
                                    .setWeight(2))
                              .add(chalkBox(List.of(
                                    chalkStack(DyeColor.BLUE, 5), EMPTY, chalkStack(DyeColor.ORANGE, 26),
                                    EMPTY, EMPTY, EMPTY,
                                    chalkStack(DyeColor.YELLOW, 0), EMPTY, EMPTY), 0)
                                    .setWeight(2))
                              .add(chalk(DyeColor.WHITE, 5))
                              .add(chalk(DyeColor.LIGHT_GRAY, 5))
                              .add(chalk(DyeColor.GRAY, 5))
                              .add(chalk(DyeColor.BLACK, 5))
                              .add(chalk(DyeColor.YELLOW, 5))
                              .add(chalk(DyeColor.LIGHT_BLUE, 5))
                              .add(chalk(DyeColor.ORANGE, 5))
                              .add(EmptyLootItem.emptyItem().setWeight(60))
                  ));

            context.accept(Chalk.LootTables.SIMPLE_DUNGEON_CHALKS,
                  LootTable.lootTable().withPool(
                        LootPool.lootPool()
                              .setRolls(ContextIntProviders.exactly(2))
                              .add(chalkBox(List.of(
                                    chalkStack(DyeColor.WHITE, 23), EMPTY, EMPTY, EMPTY,
                                    chalkStack(DyeColor.LIGHT_GRAY, 12), EMPTY, EMPTY, chalkStack(DyeColor.RED, 0),
                                    EMPTY), 0)
                                    .setWeight(2))
                              .add(chalkBox(List.of(
                                    EMPTY, chalkStack(DyeColor.LIGHT_GRAY, 0), EMPTY,
                                    chalkStack(DyeColor.WHITE, 7), EMPTY, EMPTY,
                                    chalkStack(DyeColor.LIGHT_BLUE, 15), EMPTY, EMPTY,
                                    Optional.of(new ItemStackTemplate(Items.GLOWSTONE_DUST, 7))), 2)
                                    .setWeight(2))
                              .add(chalkBox(List.of(
                                    chalkStack(DyeColor.BLUE, 5), EMPTY, chalkStack(DyeColor.PINK, 50),
                                    EMPTY, EMPTY, EMPTY,
                                    chalkStack(DyeColor.YELLOW, 0), EMPTY, EMPTY,
                                    Optional.of(new ItemStackTemplate(Items.GLOW_INK_SAC))), 5)
                                    .setWeight(2))
                              .add(chalkBox(List.of(
                                    EMPTY, chalkStack(DyeColor.MAGENTA, 6), EMPTY,
                                    EMPTY, chalkStack(DyeColor.CYAN, 0), EMPTY,
                                    chalkStack(DyeColor.LIME, 4), EMPTY, EMPTY,
                                    EMPTY), 0)
                                    .setWeight(2))
                              .add(chalk(DyeColor.WHITE, 5))
                              .add(chalk(DyeColor.LIGHT_GRAY, 5))
                              .add(chalk(DyeColor.BLACK, 5))
                              .add(EmptyLootItem.emptyItem().setWeight(50))
                  ));

            context.accept(Chalk.LootTables.VILLAGE_CHALKS,
                  LootTable.lootTable().withPool(
                        LootPool.lootPool()
                              .setRolls(ContextIntProviders.exactly(2))
                              .add(chalk(DyeColor.WHITE, 5))
                              .add(chalk(DyeColor.LIGHT_GRAY, 4))
                              .add(chalk(DyeColor.GRAY, 3))
                              .add(chalk(DyeColor.BLACK, 2))
                              .add(chalk(DyeColor.YELLOW, 5))
                              .add(chalk(DyeColor.LIGHT_BLUE, 5))
                              .add(chalk(DyeColor.ORANGE, 5))
                              .add(EmptyLootItem.emptyItem().setWeight(50))
                  ));
        }

        private UniformContainerBase.Builder<?> chalk(DyeColor color, int weight) {
            return LootItem.lootTableItem(Chalk.Items.CHALK.get())
                  .apply(SetComponentsFunction.setComponent(DataComponents.DYED_COLOR,
                        new DyedItemColor(ChalkItem.getColorFromDye(color))))
                  .setWeight(weight);
        }

        private UniformContainerBase.Builder<?> chalkBox(List<Optional<ItemStackTemplate>> items, int glowAmount) {
            for (int slot = 0; slot < items.size(); slot++) {
                Optional<ItemStackTemplate> stack = items.get(slot);
                if (stack.isPresent() && !canChalkBoxHold(slot, stack.get())) {
                    throw new IllegalStateException("Chalk Box slot '" + slot + "' cannot hold: " + stack.get());
                }
            }

            return LootItem.lootTableItem(Chalk.Items.CHALK_BOX.get())
                  .apply(SetComponentsFunction.setComponent(Chalk.DataComponents.CHALK_BOX_CONTENTS,
                        ChalkBoxContents.fromTemplates(items, glowAmount)));
        }

        /**
         * Copy of the ChalkBoxContents method, but with changes to make it work while data is generating.
         * As we cannot access server config here, or tags.
         */
        @SuppressWarnings("deprecation")
        private boolean canChalkBoxHold(int slot, ItemStackTemplate stack) {
            if (!stack.item().value().canFitInsideContainerItems()) {
                return false;
            } else if (slot == ChalkBoxContents.GLOWINGS_SLOT) {
                return stack.item().value() == Items.GLOWSTONE_DUST || stack.item().value() == Items.GLOW_INK_SAC;
            } else {
                return stack.item().value() instanceof ChalkItem;
            }
        }

        private Optional<ItemStackTemplate> chalkStack(DyeColor color, int damage) {
            DataComponentPatch.Builder components = DataComponentPatch.builder();
            if (color != DyeColor.WHITE) {
                components.set(DataComponents.DYED_COLOR, new DyedItemColor(ChalkItem.getColorFromDye(color)));
            }
            if (damage > 0) {
                components.set(DataComponents.DAMAGE, damage);
            }
            return Optional.of(new ItemStackTemplate(Chalk.Items.CHALK.get(), 1, components.build()));
        }
    }
}
