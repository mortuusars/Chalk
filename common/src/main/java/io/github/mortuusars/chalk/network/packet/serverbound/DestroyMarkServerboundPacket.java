package io.github.mortuusars.chalk.network.packet.serverbound;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.world.block.MarkBlock;
import io.github.mortuusars.mortaar.network.packet.Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record DestroyMarkServerboundPacket(BlockPos pos, Direction face) implements Packet {
    public static final Type<DestroyMarkServerboundPacket> TYPE = new Type<>(Chalk.resource("destroy_mark"));

    public static final StreamCodec<FriendlyByteBuf, DestroyMarkServerboundPacket> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, DestroyMarkServerboundPacket::pos,
          Direction.STREAM_CODEC, DestroyMarkServerboundPacket::face,
          DestroyMarkServerboundPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean handle(PacketFlow flow, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            Chalk.LOGGER.error("Cannot handle {}: player is not ServerPlayer.", TYPE.id());
            return false;
        }

        if (!player.canInteractWithBlock(pos, 1.0)) {
            Chalk.LOGGER.error("Cannot handle {}: player is not in range: Player: [{}], Mark: [{}].",
                  TYPE.id(), player.blockPosition().toShortString(), pos.toShortString());
            return false;
        }

        if (player.blockActionRestricted(player.level(), pos, serverPlayer.gameMode.getGameModeForPlayer())) {
            Chalk.LOGGER.error("Cannot handle {}: block action restricted: Player: [{}], Mark: [{}].",
                  TYPE.id(), player.blockPosition().toShortString(), pos.toShortString());
            return false;
        }

        if (serverPlayer.level().getBlockState(pos).getBlock() instanceof MarkBlock markBlock) {
            markBlock.removeMarkWithEffects(player.level(), pos, face);
        }

        return true;
    }
}