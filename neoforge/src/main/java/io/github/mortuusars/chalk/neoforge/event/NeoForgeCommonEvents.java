package io.github.mortuusars.chalk.neoforge.event;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.world.chalk.ChalkColors;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = Chalk.ID)
public class NeoForgeCommonEvents {
    @SubscribeEvent
    public static void addDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(Chalk.Registries.MARK_SYMBOL, MarkSymbol.DIRECT_CODEC, MarkSymbol.DIRECT_CODEC);
    }

    @SubscribeEvent
    private static void onCreativeTabsBuild(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(new ItemStack(Chalk.Items.CHALK.get()));

            if (Config.Server.ADD_DYED_CHALKS_TO_TAB.get()) {
                for (DyeColor dyeColor : ChalkColors.ORDERED_DYE_COLORS) {
                    if (dyeColor == DyeColor.WHITE) {
                        continue;
                    }

                    ItemStack stack = new ItemStack(Chalk.Items.CHALK.get());
                    int color = Chalk.Items.CHALK.get().getColorFromDye(stack, dyeColor);
                    stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, true));
                    event.accept(stack);
                }
            }

            event.accept(Chalk.Items.CHALK_BOX.get());
        }
    }

    @SubscribeEvent
    public static void configLoaded(ModConfigEvent.Loading event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER) {
            Config.Server.loading();
        }
    }

    @SubscribeEvent
    public static void configLoaded(ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER) {
            Config.Server.reloading();
        }
    }
}