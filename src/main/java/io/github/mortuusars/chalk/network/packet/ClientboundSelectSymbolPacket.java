package io.github.mortuusars.chalk.network.packet;

import io.github.mortuusars.chalk.client.gui.SymbolSelectScreen;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record ClientboundSelectSymbolPacket(List<MarkSymbol> unlockedSymbols) {
    public static ClientboundSelectSymbolPacket fromBuffer(FriendlyByteBuf buffer) {
        List<MarkSymbol> symbols = new ArrayList<>();
        int count = buffer.readInt();

        for (int i = 0; i < count; i++) {
            symbols.add(buffer.readEnum(MarkSymbol.class));
        }

        return new ClientboundSelectSymbolPacket(symbols);
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(unlockedSymbols.size());
        for (MarkSymbol unlockedSymbol : unlockedSymbols) {
            buffer.writeEnum(unlockedSymbol);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        MarkDrawingContext storedContext = MarkDrawingContext.getStoredContext();
        if (storedContext == null)
            throw new IllegalStateException("Stored MarkDrawingContext was null.");

        SymbolSelectScreen symbolSelectScreen = new SymbolSelectScreen(unlockedSymbols, storedContext, InteractionHand.MAIN_HAND);
        Minecraft.getInstance().setScreen(symbolSelectScreen);

        MarkDrawingContext.clearStoredContext();
    }
}
