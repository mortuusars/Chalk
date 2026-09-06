package io.github.mortuusars.chalk.network.packet.clientbound;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.network.handler.ClientPacketHandler;
import io.github.mortuusars.chalk.world.chalk.MarkDrawingContext;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SelectSymbolAndDrawMarkClientboundPacket(List<Holder<MarkSymbol>> availableSymbols, MarkDrawingContext context) implements Packet {
    public static final Type<SelectSymbolAndDrawMarkClientboundPacket> TYPE = new Type<>(Chalk.resource("select_symbol_and_draw_mark"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectSymbolAndDrawMarkClientboundPacket> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.holderRegistry(Chalk.Registries.MARK_SYMBOL).apply(ByteBufCodecs.list()), SelectSymbolAndDrawMarkClientboundPacket::availableSymbols,
          MarkDrawingContext.STREAM_CODEC, SelectSymbolAndDrawMarkClientboundPacket::context,
          SelectSymbolAndDrawMarkClientboundPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ClientPacketHandler.handleSelectSymbolAndDrawMark(this);
        return true;
    }
}
