package io.github.mortuusars.chalk.network.handler;

import io.github.mortuusars.chalk.client.gui.screens.SymbolSelectScreen;
import io.github.mortuusars.chalk.network.packet.clientbound.SelectSymbolAndDrawMarkClientboundPacket;
import io.github.mortuusars.chalk.world.item.MarkDrawable;
import io.github.mortuusars.mortaar.client.Minecrft;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {
    public static void handleSelectSymbolAndDrawMark(SelectSymbolAndDrawMarkClientboundPacket packet) {
        if (Minecraft.getInstance().player == null
              || Minecraft.getInstance().level == null
              || !(Minecraft.getInstance().player.getItemInHand(packet.context().hand()).getItem() instanceof MarkDrawable)) {
            return;
        }

        Minecrft.get().gui.setScreen(new SymbolSelectScreen(packet.availableSymbols(), packet.context()));
    }
}
