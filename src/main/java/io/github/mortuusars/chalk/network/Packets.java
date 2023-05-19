package io.github.mortuusars.chalk.network;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.network.packet.ClientboundSelectSymbolPacket;
import io.github.mortuusars.chalk.network.packet.ServerboundDrawMarkPacket;
import io.github.mortuusars.chalk.network.packet.ServerboundOpenChalkBoxPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Packets {
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Chalk.resource("packets"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        CHANNEL.messageBuilder(ServerboundDrawMarkPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundDrawMarkPacket::toBuffer)
                .decoder(ServerboundDrawMarkPacket::fromBuffer)
                .consumerMainThread(ServerboundDrawMarkPacket::handle)
                .add();

        CHANNEL.messageBuilder(ServerboundOpenChalkBoxPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundOpenChalkBoxPacket::toBuffer)
                .decoder(ServerboundOpenChalkBoxPacket::fromBuffer)
                .consumerMainThread(ServerboundOpenChalkBoxPacket::handle)
                .add();

        CHANNEL.messageBuilder(ClientboundSelectSymbolPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundSelectSymbolPacket::toBuffer)
                .decoder(ClientboundSelectSymbolPacket::fromBuffer)
                .consumerMainThread(ClientboundSelectSymbolPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
