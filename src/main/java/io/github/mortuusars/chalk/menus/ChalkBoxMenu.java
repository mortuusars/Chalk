package io.github.mortuusars.chalk.menus;

import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.items.ChalkBox;
import io.github.mortuusars.chalk.setup.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChalkBoxMenu extends AbstractContainerMenu {

    private final ItemStack chalkBoxStack;

    public ChalkBoxMenu(final int pContainerId, final Inventory playerInventory, ItemStack chalkBoxStack, @Nullable IItemHandler itemHandler) {
        super(ModMenus.CHALK_BOX.get(), pContainerId);
        this.chalkBoxStack = chalkBoxStack;

        final boolean glowingEnabled = CommonConfig.CHALK_BOX_GLOWING.get();

        // Order of adding slots is kinda important. QuickMoveStack depends on correct order.

        int slotsYPos = glowingEnabled ? 18 : 33;

        // Add chalk slots
        int index = 0;
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 4; column++) {

                if (index >= ChalkBox.GLOWING_ITEM_SLOT_ID)
                    throw new IllegalStateException("Chalk slot ids should go before Glowing Item slot id and not exceed it.");

                addSlot(new SlotItemHandler(itemHandler, index++, column * 18 + 53, row * 18 + slotsYPos));
            }
        }

        if (glowingEnabled){
            addSlot(new SlotItemHandler(itemHandler, ChalkBox.GLOWING_ITEM_SLOT_ID, 80, 68));
        }

        addPlayerSlots(playerInventory);
    }

    public static ChalkBoxMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf dataBuffer) {
        // Creating ItemHandler from copied stack when on client.
        ItemStack chalkBoxStack = dataBuffer.readItem().copy();
        ChalkBoxItemStackHandler itemHandler = new ChalkBoxItemStackHandler(chalkBoxStack);
        return new ChalkBoxMenu(containerID, playerInventory, chalkBoxStack, itemHandler);
    }

    public int getGlowingUses(){
        return ChalkBox.getGlowingUses(chalkBoxStack);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotItemStack = slot.getItem();
            itemstack = slotItemStack.copy();
            if (index < ChalkBox.SLOTS){ // From Chalk Box to player inventory.
                if (!this.moveItemStackTo(slotItemStack, ChalkBox.SLOTS, this.slots.size(), true))
                    return ItemStack.EMPTY;
            }
            else if (!this.moveItemStackTo(slotItemStack, 0, ChalkBox.SLOTS, false)) // From player inventory to box.
                return ItemStack.EMPTY;


            if (slotItemStack.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    private void addPlayerSlots(Inventory playerInventory) {
        //Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = (column + row * 9) + 9;
                addSlot(new Slot(playerInventory, index, column * 18 + 8, 98 + row * 18));
            }
        }

        //Hotbar
        for (int column = 0; column < 9; column++)
            addSlot(new Slot(playerInventory, column, column * 18 + 8, 156));

    }
}
