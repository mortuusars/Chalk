package io.github.mortuusars.chalk;

import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.items.ChalkBoxItem;
import io.github.mortuusars.chalk.items.ChalkItem;
import io.github.mortuusars.chalk.loot.LootTableModification;
import io.github.mortuusars.chalk.menus.ChalkBoxMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

@Mod("chalk")
public class Chalk
{
    public static final String ID = "chalk";
    public static final Logger LOGGER = LogManager.getLogger();

    public Chalk() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Blocks.BLOCKS.register(modEventBus);
        Items.ITEMS.register(modEventBus);
        Menus.MENUS.register(modEventBus);
        SoundEvents.SOUND_EVENTS.register(modEventBus);

        MinecraftForge.EVENT_BUS.addListener(LootTableModification::LootTablesLoad);
        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.addListener(Chalk::onRightClickBlock);

    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // Testing
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(ID, path);
    }

    public static class Blocks {
        private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
        public static final HashMap<DyeColor, RegistryObject<ChalkMarkBlock>> MARKS = new HashMap<>();

        static {
            for (DyeColor color : DyeColor.values()) {
                MARKS.put(color, BLOCKS.register(color + "_chalk_mark",
                        () -> new ChalkMarkBlock(color, BlockBehaviour.Properties.of(Material.REPLACEABLE_FIREPROOF_PLANT, color.getMaterialColor())
                                .instabreak()
                                .noOcclusion()
                                .noCollission()
                                .emissiveRendering((state, level, pos) -> state.getValue(ChalkMarkBlock.GLOWING))
                                .sound(SoundType.GRAVEL))));
            }
        }

        public static ChalkMarkBlock getMarkBlock(DyeColor color){
            return MARKS.get(color).get();
        }
    }

    public static class Items {
        private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Chalk.ID);

        public static HashMap<DyeColor, RegistryObject<ChalkItem>> CHALKS = new HashMap<>();

        public static final RegistryObject<ChalkBoxItem> CHALK_BOX = ITEMS.register("chalk_box",
                () -> new ChalkBoxItem(new Item.Properties()
                        .tab(CreativeModeTab.TAB_TOOLS)
                        .stacksTo(1)));

        static {
            for (DyeColor color : DyeColor.values()) {
                CHALKS.put(color, ITEMS.register(color + "_chalk", () -> new ChalkItem(color, new Item.Properties())));
            }
        }

        public static ChalkItem getChalk(DyeColor color){
            return CHALKS.get(color).get();
        }
    }

    public static class Tags {
        public static final class Items {
            public static final TagKey<Item> CHALKS = ItemTags.create(new ResourceLocation("chalk:chalks"));
            public static final TagKey<Item> FORGE_CHALKS = ItemTags.create(new ResourceLocation("forge:chalks"));
            public static final TagKey<Item> GLOWINGS = ItemTags.create(new ResourceLocation("chalk:glowings"));
        }

        public static final class Blocks {
            public static final TagKey<Block> CHALK_MARKS = BlockTags.create(new ResourceLocation("chalk:chalk_marks"));
        }
    }

    public static class Menus {
        private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Chalk.ID);

        public static final RegistryObject<MenuType<ChalkBoxMenu>> CHALK_BOX = MENUS.register("chalk_box",
                () -> IForgeMenuType.create(ChalkBoxMenu::fromBuffer));
    }

    public static class SoundEvents {
        private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Chalk.ID);

        public static final RegistryObject<SoundEvent> CHALK_BROKEN = SOUND_EVENTS.register("item.chalk_broken",
                () -> new SoundEvent(Chalk.resource("item.chalk_broken")));
        public static final RegistryObject<SoundEvent> CHALK_BOX_CHANGE = SOUND_EVENTS.register("item.chalk_box_change",
                () -> new SoundEvent(Chalk.resource("item.chalk_box_change")));
        public static final RegistryObject<SoundEvent> CHALK_BOX_OPEN = SOUND_EVENTS.register("item.chalk_box_open",
                () -> new SoundEvent(Chalk.resource("item.chalk_box_open")));
        public static final RegistryObject<SoundEvent> MARK_DRAW = SOUND_EVENTS.register("item.chalk_draw",
                () -> new SoundEvent(Chalk.resource("item.chalk_draw")));
        public static final RegistryObject<SoundEvent> MARK_GLOW_APPLIED = SOUND_EVENTS.register("block.mark_glow_applied",
                () -> new SoundEvent(Chalk.resource("block.mark_glow_applied")));
        public static final RegistryObject<SoundEvent> MARK_REMOVED = SOUND_EVENTS.register("block.mark_removed",
                () -> new SoundEvent(Chalk.resource("block.mark_removed")));
    }
}
