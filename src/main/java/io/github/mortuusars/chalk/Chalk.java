package io.github.mortuusars.chalk;

import io.github.mortuusars.chalk.setup.ModBlocks;
import io.github.mortuusars.chalk.setup.Registry;
import io.github.mortuusars.chalk.setup.StartupClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("chalk")
public class Chalk
{
    public static final String MOD_ID = "chalk";

    public static final Logger LOGGER = LogManager.getLogger();

    public Chalk() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.register(StartupClient.class);

        Registry.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
}
