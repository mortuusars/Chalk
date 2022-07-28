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
    private final IItemHandler itemHandler;

    public ChalkBoxMenu(final int pContainerId, final Inventory playerInventory, ItemStack chalkBoxStack, @Nullable IItemHandler itemHandler) {
        super(ModMenus.CHALK_BOX.get(), pContainerId);
        this.chalkBoxStack = chalkBoxStack;
        this.itemHandler = itemHandler;

        addPlayerSlots(playerInventory);

        // Add chalk slots
        int index = 0;
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 4; column++) {
                addSlot(new SlotItemHandler(itemHandler, index++, column * 18 + 53, row * 18 + 27));
            }
        }

        if (CommonConfig.CHALK_BOX_GLOWING.get()){
            addSlot(new SlotItemHandler(itemHandler, ChalkBox.GLOWING_ITEM_SLOT_ID, 26, 36));
        }
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
            if (index < ChalkBox.SLOTS){
                if (!this.moveItemStackTo(slotItemStack, ChalkBox.SLOTS, this.slots.size(), true))
                    return ItemStack.EMPTY;
            }
            else if (!this.moveItemStackTo(slotItemStack, 0, ChalkBox.SLOTS, false))
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
        //Hotbar
        for (int column = 0; column < 9; column++)
            addSlot(new Slot(playerInventory, column, column * 18 + 8, 142));

        //Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = (column + row * 9) + 9;
                addSlot(new Slot(playerInventory, index, column * 18 + 8, 84 + row * 18));
            }
        }
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);

        // Update ChalkBoxStack with current items:
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ChalkBox.setSlot(chalkBoxStack, i, itemHandler.getStackInSlot(i));
        }
    }
}
