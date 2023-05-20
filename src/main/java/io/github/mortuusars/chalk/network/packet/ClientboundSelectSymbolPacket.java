package io.github.mortuusars.chalk.network.packet;

import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.network.packet.handler.ClientsideOpenSymbolSelectScreenHandler;
import net.minecraft.network.FriendlyByteBuf;
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
        ClientsideOpenSymbolSelectScreenHandler.handle(unlockedSymbols);
    }
}
