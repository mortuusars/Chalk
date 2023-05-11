package io.github.mortuusars.chalk.network.packet;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.items.ChalkBoxItem;
import io.github.mortuusars.chalk.items.ChalkItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public record ServerboundDrawMarkPacket(Mark mark, BlockPos markBlockPos, InteractionHand drawingHand) {
    public static ServerboundDrawMarkPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ServerboundDrawMarkPacket(
                Mark.fromBuffer(buffer),
                buffer.readBlockPos(),
                buffer.readEnum(InteractionHand.class));
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        mark.toBuffer(buffer);
        buffer.writeBlockPos(markBlockPos);
        buffer.writeEnum(drawingHand);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();

        if (player == null) {
            Chalk.LOGGER.error("DrawMarkPacket cannot be handled: player was null.");
            return false;
        }

        ItemStack itemInHand = player.getItemInHand(drawingHand);
        if (!(itemInHand.getItem() instanceof ChalkItem || itemInHand.getItem() instanceof ChalkBoxItem)) {
            Chalk.LOGGER.error("Item in player's hand cannot draw a mark.");
            return true;
        }

        Level level = player.level;
        BlockState existingState = level.getBlockState(markBlockPos);

        if (!(existingState.isAir() || existingState.getBlock() instanceof ChalkMarkBlock)) {
            Chalk.LOGGER.error("Cannot draw at this block.");
            return true;
        }

        // DRAW
        if (level.setBlock(markBlockPos, mark.createBlockState(), Block.UPDATE_ALL_IMMEDIATE)) {
            double pX = markBlockPos.getX() + 0.5;
            double pY = markBlockPos.getY() + 0.5;
            double pZ = markBlockPos.getZ() + 0.5;
            level.playSound(null, pX, pY, pZ, Chalk.SoundEvents.MARK_DRAW.get(),
                    SoundSource.BLOCKS, 0.7f,  new Random().nextFloat() * 0.2f + 0.8f);

            //TODO: damage chalk.
        }

        return true;
    }
}
