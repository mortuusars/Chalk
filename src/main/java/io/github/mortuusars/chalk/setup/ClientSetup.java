package io.github.mortuusars.chalk.setup;

import io.github.mortuusars.chalk.render.Rendering;
import net.minecraftforge.eventbus.api.IEventBus;

public class ClientSetup {

    private IEventBus _modEvent_bus;

    public ClientSetup(IEventBus modEventBus){
        _modEvent_bus = modEventBus;
    }

    public void registerClientOnlyEvents(){
        _modEvent_bus.register(Rendering.class);
    }
}
