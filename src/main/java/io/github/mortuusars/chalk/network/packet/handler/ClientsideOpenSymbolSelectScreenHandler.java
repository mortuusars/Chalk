package io.github.mortuusars.chalk.network.packet.handler;

import io.github.mortuusars.chalk.client.gui.SymbolSelectScreen;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import net.minecraft.client.Minecraft;

import java.util.List;

public class ClientsideOpenSymbolSelectScreenHandler {
    public static void handle(List<MarkSymbol> unlockedSymbols) {
        MarkDrawingContext storedContext = MarkDrawingContext.getStoredContext();
        if (storedContext == null)
            throw new IllegalStateException("Stored MarkDrawingContext was null.");

        SymbolSelectScreen symbolSelectScreen = new SymbolSelectScreen(unlockedSymbols, storedContext);
        Minecraft.getInstance().setScreen(symbolSelectScreen);

        MarkDrawingContext.clearStoredContext();
    }
}
