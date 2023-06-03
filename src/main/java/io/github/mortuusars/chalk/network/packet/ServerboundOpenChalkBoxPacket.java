package io.github.mortuusars.chalk.network.packet;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.items.ChalkBoxItem;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record ServerboundOpenChalkBoxPacket(int slotID) {
    public static ServerboundOpenChalkBoxPacket fromBuffer(FriendlyByteBuf buffer) {
        return new ServerboundOpenChalkBoxPacket(buffer.readInt());
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(slotID);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        @Nullable ServerPlayer player = context.getSender();

        if (player == null) {
            Chalk.LOGGER.error("ServerboundOpenChalkBoxPacket cannot be handled: player was null.");
            return false;
        }

        ItemStack chalkBox = player.getInventory().getItem(slotID);
        if (chalkBox.isEmpty() || !(chalkBox.getItem() instanceof ChalkBoxItem)) {
            Chalk.LOGGER.error("ServerboundOpenChalkBoxPacket cannot be handled: wrong item in slot. [" + chalkBox + "]");
            return false;
        }

        ChalkBoxItem.openGUI(player, chalkBox);
        return true;
    }
}
