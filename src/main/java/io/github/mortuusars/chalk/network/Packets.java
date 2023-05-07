package io.github.mortuusars.chalk.network;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.network.packet.DrawMarkPacket;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
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
        CHANNEL.messageBuilder(DrawMarkPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DrawMarkPacket::toBuffer)
                .decoder(DrawMarkPacket::fromBuffer)
                .consumerMainThread(DrawMarkPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }
}
