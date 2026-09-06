package io.github.mortuusars.chalk;

import io.github.mortuusars.chalk.advancements.trigger.MarkDrawnTrigger;
import io.github.mortuusars.chalk.advancements.trigger.ConsecutiveSleepingTrigger;
import io.github.mortuusars.chalk.advancements.trigger.MarkGlowingTrigger;
import io.github.mortuusars.chalk.network.packet.clientbound.SelectSymbolAndDrawMarkClientboundPacket;
import io.github.mortuusars.chalk.network.packet.serverbound.DestroyMarkServerboundPacket;
import io.github.mortuusars.chalk.network.packet.serverbound.DrawMarkServerboundPacket;
import io.github.mortuusars.chalk.world.block.MarkBlockEntity;
import io.github.mortuusars.chalk.world.block.MarkBlock;
import io.github.mortuusars.chalk.world.block.OldChalkMarkBlock;
import io.github.mortuusars.chalk.world.block.OldMarkBlockEntity;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.chalk.world.item.ChalkBoxItem;
import io.github.mortuusars.chalk.world.item.OldChalkItem;
import io.github.mortuusars.chalk.world.inventory.ChalkBoxMenu;
import io.github.mortuusars.chalk.world.chalk.ChalkColors;
import io.github.mortuusars.chalk.world.item.ChalkItem;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import io.github.mortuusars.mortaar.Register;
import io.github.mortuusars.mortaar.Registrar;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Chalk {
    public static final String ID = "chalk";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final Registrar REGISTRAR = Register.registrar(ID);

    public static void init() {
        Blocks.init();
        BlockEntityTypes.init();
        Items.init();
        DataComponents.init();
        CriteriaTriggers.init();
        MenuTypes.init();
        SoundEvents.init();

        Register.serverboundPacket(DrawMarkServerboundPacket.TYPE, DrawMarkServerboundPacket.STREAM_CODEC);
        Register.serverboundPacket(DestroyMarkServerboundPacket.TYPE, DestroyMarkServerboundPacket.STREAM_CODEC);

        Register.clientboundPacket(SelectSymbolAndDrawMarkClientboundPacket.TYPE, SelectSymbolAndDrawMarkClientboundPacket.STREAM_CODEC);
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static class Blocks {
        public static final Supplier<MarkBlock> MARK = REGISTRAR.block("mark",
              () -> new MarkBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.DESTROY)
                    .replaceable()
                    .instabreak()
                    .noOcclusion()
                    .noCollission()
                    .noTerrainParticles()
                    .lightLevel(state -> state.getValue(MarkBlock.GLOWING) ? 5 : 0)
                    .sound(SoundType.NETHER_WART)));

        @SuppressWarnings("removal")
        @Deprecated(since = "2.0.0", forRemoval = true)
        public static final Map<DyeColor, Supplier<OldChalkMarkBlock>> MARKS = Util.make(new LinkedHashMap<>(), map -> {
            for (DyeColor color : ChalkColors.COLORS.keySet()) {
                map.put(color, REGISTRAR.block(color + "_chalk_mark",
                        () -> new OldChalkMarkBlock(color, BlockBehaviour.Properties.of()
                                .mapColor(color)
                                .pushReaction(PushReaction.DESTROY)
                                .instabreak()
                                .noOcclusion()
                                .noCollission()
                                .randomTicks()
                                .sound(SoundType.NETHER_WART))));
            }
        });

        static void init() {
        }
    }

    public static class BlockEntityTypes {
        public static final Supplier<BlockEntityType<MarkBlockEntity>> MARK = REGISTRAR.blockEntityType("mark",
              () -> REGISTRAR.newBlockEntityType(MarkBlockEntity::new, Blocks.MARK.get()));
        @SuppressWarnings("removal")
        @Deprecated(since = "2.0.0", forRemoval = true)
        public static final Supplier<BlockEntityType<OldMarkBlockEntity>> CHALK_MARK = REGISTRAR.blockEntityType("chalk_mark",
              () -> REGISTRAR.newBlockEntityType(OldMarkBlockEntity::new, Blocks.MARKS.values().stream().map(Supplier::get).toArray(Block[]::new)));

        static void init() {
        }
    }

    public static class Foods {
        public static final FoodProperties CHALK = new FoodProperties.Builder()
              .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0), 1.0F)
              .alwaysEdible()
              .build();
    }

    public static class Items {
        public static final Supplier<ChalkItem> CHALK = REGISTRAR.item("chalk",
              () -> new ChalkItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(64)
                    .food(Foods.CHALK)));

        public static final Supplier<ChalkBoxItem> CHALK_BOX = REGISTRAR.item("chalk_box",
              () -> new ChalkBoxItem(new Item.Properties()
                    .stacksTo(1)));

        @SuppressWarnings("removal")
        @Deprecated(since = "2.0.0", forRemoval = true)
        public static Map<DyeColor, Supplier<OldChalkItem>> CHALKS = Util.make(new LinkedHashMap<>(), map -> {
            for (DyeColor color : ChalkColors.COLORS.keySet()) {
                map.put(color, REGISTRAR.item(color + "_chalk", () -> new OldChalkItem(color, new Item.Properties()
                      .stacksTo(1)
                      .durability(64))));
            }
        });

        static void init() {
        }
    }

    public static class DataComponents {
        public static final DataComponentType<ChalkBoxContents> CHALK_BOX_CONTENTS = REGISTRAR.dataComponentType("chalk_box_contents",
              builder -> builder.persistent(ChalkBoxContents.CODEC).networkSynchronized(ChalkBoxContents.STREAM_CODEC).cacheEncoding());

        static void init() {
        }
    }

    public static class MenuTypes {
        public static final Supplier<MenuType<ChalkBoxMenu>> CHALK_BOX = REGISTRAR.menuType("chalk_box", ChalkBoxMenu::fromNetwork);

        static void init() {
        }
    }

    public static class CriteriaTriggers {
        public static final Supplier<ConsecutiveSleepingTrigger> CONSECUTIVE_SLEEPING =
              REGISTRAR.criterionTrigger("consecutive_sleeping", ConsecutiveSleepingTrigger::new);
        public static final Supplier<MarkDrawnTrigger> MARK_DRAWN =
              REGISTRAR.criterionTrigger("mark_drawn", MarkDrawnTrigger::new);
        public static final Supplier<MarkGlowingTrigger> MARK_GLOWING =
              REGISTRAR.criterionTrigger("mark_glowing", MarkGlowingTrigger::new);

        static void init() {
        }
    }

    public static class SoundEvents {
        public static final Supplier<SoundEvent> MARK_DRAWN = REGISTRAR.soundEvent("item.chalk.draw",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.chalk.draw")));
        public static final Supplier<SoundEvent> MARK_REMOVED = REGISTRAR.soundEvent("block.mark.removed",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("block.mark.removed")));
        public static final Supplier<SoundEvent> CHALK_BOX_OPEN = REGISTRAR.soundEvent("item.chalk_box.open",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.chalk_box.open")));
        public static final Supplier<SoundEvent> CHALK_BOX_CLOSE = REGISTRAR.soundEvent("item.chalk_box.close",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.chalk_box.close")));
        public static final Supplier<SoundEvent> CHALK_BOX_CHANGE = REGISTRAR.soundEvent("item.chalk_box.change",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.chalk_box.change")));
        public static final Supplier<SoundEvent> GLOW_APPLIED = REGISTRAR.soundEvent("item.glow_applied",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("item.glow_applied")));
        public static final Supplier<SoundEvent> GLOWING = REGISTRAR.soundEvent("ambient.glowing",
              () -> SoundEvent.createVariableRangeEvent(Chalk.resource("ambient.glowing")));

        static void init() {
        }
    }

    public static class Registries {
        public static final ResourceKey<Registry<MarkSymbol>> MARK_SYMBOL = ResourceKey.createRegistryKey(resource("mark_symbol"));
    }

    public static class LootTables {
        public static final ResourceKey<LootTable> ABANDONED_MINESHAFT_CHALKS =
              ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Chalk.resource("chests/abandoned_mineshaft_chalks"));
        public static final ResourceKey<LootTable> DESERT_PYRAMID_CHALKS =
              ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Chalk.resource("chests/desert_pyramid_chalks"));
        public static final ResourceKey<LootTable> SIMPLE_DUNGEON_CHALKS =
              ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Chalk.resource("chests/simple_dungeon_chalks"));
        public static final ResourceKey<LootTable> VILLAGE_CHALKS =
              ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, Chalk.resource("chests/village_chalks"));
    }

    public static class Tags {
        public static final class Items {
            public static final TagKey<Item> GLOWINGS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, Chalk.resource("glowings"));
        }

        public static final class Blocks {
            public static final TagKey<Block> CHALK_CANNOT_DRAW_ON = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Chalk.resource("chalk_cannot_draw_on"));
        }
    }
}
