package io.github.mortuusars.chalk.event;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.network.Packets;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Chalk.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvents {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Packets.register();
            Chalk.CriteriaTriggers.register();
        });
    }
}
