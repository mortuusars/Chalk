package io.github.mortuusars.chalk;

import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.setup.Registry;
import io.github.mortuusars.chalk.setup.ClientSetup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

@Mod("chalk")
public class Chalk
{
    public static final String MOD_ID = "chalk";
    public static final Logger LOGGER = LogManager.getLogger();

    public Chalk() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        final ClientSetup clientSetup = new ClientSetup(modEventBus);
        Registry.register(modEventBus);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> clientSetup::registerClientOnlyEvents);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
