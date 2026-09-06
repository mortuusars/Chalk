package io.github.mortuusars.chalk.network.packet.serverbound;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.chalk.MarkDrawingContext;
import io.github.mortuusars.chalk.world.chalk.symbol.MarkSymbol;
import io.github.mortuusars.chalk.world.item.MarkDrawable;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record DrawMarkServerboundPacket(Holder<MarkSymbol> symbol, MarkDrawingContext drawingContext) implements Packet {
    public static final CustomPacketPayload.Type<DrawMarkServerboundPacket> TYPE = new CustomPacketPayload.Type<>(Chalk.resource("draw_mark"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DrawMarkServerboundPacket> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.holderRegistry(Chalk.Registries.MARK_SYMBOL), DrawMarkServerboundPacket::symbol,
          MarkDrawingContext.STREAM_CODEC, DrawMarkServerboundPacket::drawingContext,
          DrawMarkServerboundPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        ItemStack itemInHand = player.getItemInHand(drawingContext.hand());
        if (!(itemInHand.getItem() instanceof MarkDrawable drawable)) {
            Chalk.LOGGER.error("{} is not a drawing tool.", itemInHand);
            return false;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            Chalk.LOGGER.error("{} is not a server player.", player);
            return false;
        }

        if (!drawable.isSymbolAvailable(serverPlayer, drawingContext, symbol)) {
            Chalk.LOGGER.error("{} tried to draw symbol '{}', but doesn't have it unlocked.", player,
                  symbol.unwrapKey().map(k -> k.location().toString()).orElse("<unknown symbol>"));
            return false;
        }

        drawable.drawMark(player, drawingContext, drawable.createMark(player, drawingContext, itemInHand, symbol));
        return true;
    }
}