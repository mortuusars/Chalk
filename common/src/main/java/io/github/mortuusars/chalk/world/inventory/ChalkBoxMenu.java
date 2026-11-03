package io.github.mortuusars.chalk.world.inventory;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ChalkBoxMenu extends AbstractInHandContainerMenu {
    protected boolean containerInitialized;

    public ChalkBoxMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(Chalk.MenuTypes.CHALK_BOX.get(), containerId, playerInventory, hand);
        addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(@NonNull AbstractContainerMenu container, int slotIndex, @NonNull ItemStack itemStack) {
                containerChanged(slotIndex, itemStack);
            }

            @Override
            public void dataChanged(@NonNull AbstractContainerMenu container, int id, int value) {

            }
        });
    }

    @Override
    protected void init() {
        playerSlotsY = 84;
        super.init();
    }

    @Override
    public void initializeContents(int stateId, @NonNull List<ItemStack> items, @NonNull ItemStack carried) {
        super.initializeContents(stateId, items, carried);
        containerInitialized = true;
    }

    @Override
    protected Container createContainer() {
        List<ItemStack> items = new ArrayList<>(ChalkBoxContents.of(getItemInHand()).copyItems());
        while (items.size() < ChalkBoxContents.SLOTS) {
            items.add(ItemStack.EMPTY);
        }
        return new SimpleContainer(items.stream().limit(ChalkBoxContents.SLOTS).toArray(ItemStack[]::new));
    }

    protected void containerChanged(int slotIndex, ItemStack itemStack) {
        if (!containerInitialized && getPlayer().level().isClientSide()) {
            return;
        }

        if (slotIndex >= ChalkBoxContents.SLOTS) {
            return;
        }

        getContents().mutable()
              .setItems(getContainer())
              .updateGlow()
              .toImmutable(getItemInHand());
    }

    @Override
    protected void addContainerSlots() {
        int chalkSlotsX = 62;
        int chalkSlotsY = 17;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int index = column + row * 3;
                int x = chalkSlotsX + column * 18;
                int y = chalkSlotsY + row * 18;

                addSlot(new Slot(getContainer(), index, x, y) {
                    @Override
                    public boolean mayPlace(@NonNull ItemStack stack) {
                        return ChalkBoxContents.canHold(getContainerSlot(), stack);
                    }
                });
            }
        }

        addSlot(new Slot(getContainer(), ChalkBoxContents.GLOWINGS_SLOT, 134, 35) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return isGlowEnabled() && ChalkBoxContents.canHold(getContainerSlot(), stack);
            }

            @Override
            public boolean isActive() {
                return isGlowEnabled();
            }

            @Override
            public void set(@NotNull ItemStack stack) {
                if (getPlayer().level().isClientSide() && this.getItem().isEmpty()
                      && getGlow() <= 0 && stack.is(Chalk.Tags.Items.GLOWINGS)) {
                    Vec3 pos = getPlayer().position();
                    getPlayer().level().playSound(getPlayer(), pos.x, pos.y, pos.z, Chalk.SoundEvents.GLOW_APPLIED.get(), SoundSource.PLAYERS, 1f, 1f);
                    getPlayer().level().playSound(getPlayer(), pos.x, pos.y, pos.z, Chalk.SoundEvents.GLOWING.get(), SoundSource.PLAYERS, 1f, 1f);
                }

                super.set(stack);
            }
        });
    }

    @Override
    public boolean clickMenuButton(@NonNull Player player, int id) {
        if (id >= 100 && id < 100 + ChalkBoxContents.CHALK_SLOTS) {
            setSelectedSlot(id - 100);
            return true;
        }
        return false;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        player.playSound(Chalk.SoundEvents.CHALK_BOX_CLOSE.get(), 0.85f, 0.9f + player.level().getRandom().nextFloat() * 0.2f);

        if (player.isCreative() && !player.level().isClientSide()) {
            player.getInventory().setItem(getUsedSlot(), getItemInHand());
        }

        // Fixes inventory not syncing after closing:
        player.inventoryMenu.resumeRemoteUpdates();
    }

    public ChalkBoxContents getContents() {
        return ChalkBoxContents.of(getItemInHand());
    }

    public boolean isGlowEnabled() {
        return  Config.Server.GLOWING_ENABLED.get() && Config.Server.CHALK_BOX_GLOWING_ENABLED.get();
    }

    public int getGlow() {
        return getContents().glow();
    }

    public int getMaxGlow() {
        return Config.Server.CHALK_BOX_GLOWING_AMOUNT_PER_ITEM.get();
    }

    public int getSelectedSlot() {
        return getContents().selected();
    }

    public void setSelectedSlot(int slot) {
        if (getContents().selected() != slot) {
            getContents().mutable().setSelected(slot).toImmutable(getItemInHand());
            getPlayer().resetAttackStrengthTicker();
        }
    }

    public static ChalkBoxMenu fromNetwork(int containerID, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        return new ChalkBoxMenu(containerID, playerInventory, buffer.readEnum(InteractionHand.class));
    }
}
