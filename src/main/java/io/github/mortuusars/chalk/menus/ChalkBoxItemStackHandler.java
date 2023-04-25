package io.github.mortuusars.chalk.menus;

import io.github.mortuusars.chalk.items.ChalkBox;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChalkBoxItemStackHandler extends ItemStackHandler {
    private final ItemStack chalkBoxStack;

    public ChalkBoxItemStackHandler(ItemStack chalkBoxStack) {
        super(ChalkBox.SLOTS);
        this.chalkBoxStack = chalkBoxStack;

        List<ItemStack> contents = ChalkBox.getContents(chalkBoxStack);

        for (int index = 0; index < contents.size(); index++) {
            this.stacks.set(index, contents.get(index));
        }
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return ChalkBox.isItemValid(slot, stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        int prevGlowingUses = ChalkBox.getGlow(chalkBoxStack);

        ChalkBox.setSlot(chalkBoxStack, slot, getStackInSlot(slot));

        if (slot == ChalkBox.GLOWINGS_SLOT_INDEX && ChalkBox.getGlow(chalkBoxStack) > prevGlowingUses) {
            // Refresh glow stack:
            this.stacks.set(slot, ChalkBox.getItemInSlot(chalkBoxStack, slot));
//            ItemStack glowingStack = getStackInSlot(slot);
//            glowingStack.shrink(1);
//            setStackInSlot(slot, glowingStack);
        }
    }
}
