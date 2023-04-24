package io.github.mortuusars.chalk.menus;

import io.github.mortuusars.chalk.Chalk;
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
            stacks.set(index, contents.get(index));
        }
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot == ChalkBox.GLOWING_ITEM_SLOT_ID)
            return stack.is(Chalk.Tags.Items.GLOWING);
        else
            return stack.is(Chalk.Tags.Items.CHALK);
    }

    @Override
    protected void onContentsChanged(int slot) {
        int prevGlowingUses = ChalkBox.getGlowingUses(chalkBoxStack);

        ChalkBox.setSlot(chalkBoxStack, slot, getStackInSlot(slot));

        if (slot == ChalkBox.GLOWING_ITEM_SLOT_ID && ChalkBox.getGlowingUses(chalkBoxStack) > prevGlowingUses) {
            ItemStack glowingStack = getStackInSlot(slot);
            glowingStack.shrink(1);
            setStackInSlot(slot, glowingStack);
        }
    }
}
