package io.github.mortuusars.chalk.menus;

import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.Config;
import io.github.mortuusars.chalk.items.ChalkBox;
import io.github.mortuusars.chalk.items.ChalkBoxItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChalkBoxMenu extends AbstractContainerMenu {

    public final ItemStack chalkBoxStack;
    private final Player player;
    public Pair<Integer, Integer> chalkBoxCoords = Pair.of(Integer.MIN_VALUE, Integer.MIN_VALUE);

    private final int chalkBoxSlotId;

    public ChalkBoxMenu(final int pContainerId, final Inventory playerInventory, ItemStack chalkBoxStack, @Nullable IItemHandler itemHandler) {
        super(Chalk.Menus.CHALK_BOX.get(), pContainerId);
        this.chalkBoxStack = chalkBoxStack;
        this.player = playerInventory.player;

        final boolean glowingEnabled = Config.CHALK_BOX_GLOWING.get();

        // Order of adding slots is kinda important. QuickMoveStack depends on correct order.

        int slotsYPos = glowingEnabled ? 18 : 33;

        // To avoid duping items - we do not add slot with the chalk box. And to be extra safe - check for slot matching in #stillValid.
        chalkBoxSlotId = playerInventory.findSlotMatchingItem(chalkBoxStack);

        // Add chalk slots
        int index = 0;
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 4; column++) {

                if (index >= ChalkBox.GLOWINGS_SLOT_INDEX)
                    throw new IllegalStateException("Chalk slot ids should go before Glowing Item slot id and not exceed it.");

                addSlot(new SlotItemHandler(itemHandler, index++, column * 18 + 53, row * 18 + slotsYPos));
            }
        }

        if (glowingEnabled){
            addSlot(new SlotItemHandler(itemHandler, ChalkBox.GLOWINGS_SLOT_INDEX, 80, 68) {
                @Override
                public void set(@NotNull ItemStack stack) {
                    if (player.level instanceof ClientLevel clientLevel && this.getItem().isEmpty()
                            && ChalkBox.getGlowLevel(chalkBoxStack) <= 0 && stack.is(Chalk.Tags.Items.GLOWINGS)) {
                        Vec3 pos = player.position();
                        clientLevel.playSound(player, pos.x, pos.y, pos.z, Chalk.SoundEvents.GLOW_APPLIED.get(), SoundSource.PLAYERS, 1f, 1f);
                        clientLevel.playSound(player, pos.x, pos.y, pos.z, Chalk.SoundEvents.GLOWING.get(), SoundSource.PLAYERS, 1f, 1f);
                    }

                    super.set(stack);
                }
            });
        }

        addPlayerSlots(playerInventory);
    }

    @Override
    public void removed(@NotNull Player pPlayer) {
        super.removed(pPlayer);
        pPlayer.playSound(Chalk.SoundEvents.CHALK_BOX_CLOSE.get(), 0.85f, 0.9f + pPlayer.level.random.nextFloat() * 0.2f);

        // I still have no clue why updates are stopping when ChalkBox is opened by right click in inv.
        // But this fixes inventory not syncing after closing.
        pPlayer.inventoryMenu.resumeRemoteUpdates();
    }

    public static ChalkBoxMenu fromBuffer(int containerID, Inventory playerInventory, FriendlyByteBuf dataBuffer) {
        // Creating ItemHandler from copied stack when on client.
        ItemStack chalkBoxStack = dataBuffer.readItem().copy();
        ChalkBoxItemStackHandler itemHandler = new ChalkBoxItemStackHandler(chalkBoxStack);
        return new ChalkBoxMenu(containerID, playerInventory, chalkBoxStack, itemHandler);
    }

    public int getGlowingUses(){
        return ChalkBox.getGlowLevel(chalkBoxStack);
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
        return chalkBoxSlotId < 0 || player.getInventory().getItem(chalkBoxSlotId).getItem() instanceof ChalkBoxItem;
    }

    private void addPlayerSlots(Inventory playerInventory) {
        int chalkBoxSlotId = playerInventory.findSlotMatchingItem(chalkBoxStack);

        //Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = (column + row * 9) + 9;

                if (index == chalkBoxSlotId) {
                    chalkBoxCoords = Pair.of(column * 18 + 8, 98 + row * 18);
                    addSlot(new Slot(playerInventory, index, column * 18 + 8, 98 + row * 18) {
                        @Override
                        public boolean mayPlace(@NotNull ItemStack pStack) {
                            return false;
                        }

                        @Override
                        public boolean mayPickup(@NotNull Player pPlayer) {
                            return false;
                        }

                        @Override
                        public boolean isActive() {
                            return false;
                        }
                    });
                    continue;
                }

                addSlot(new Slot(playerInventory, index, column * 18 + 8, 98 + row * 18));
            }
        }

        //Hotbar
        for (int index = 0; index < 9; index++) {
            if (index == chalkBoxSlotId) {
                chalkBoxCoords = Pair.of(index * 18 + 8, 156);
                addSlot(new Slot(playerInventory, index, index * 18 + 8, 156) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack pStack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(@NotNull Player pPlayer) {
                        return false;
                    }

                    @Override
                    public boolean isActive() {
                        return false;
                    }
                });
                continue;
            }

            addSlot(new Slot(playerInventory, index, index * 18 + 8, 156));
        }
    }
}
