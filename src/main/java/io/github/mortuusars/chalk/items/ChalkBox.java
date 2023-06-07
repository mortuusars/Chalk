package io.github.mortuusars.chalk.items;

import com.google.common.base.Preconditions;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChalkBox {

    public static final int SLOTS = 9;
    public static final int CHALK_SLOTS = 8;
    public static final int GLOWINGS_SLOT_INDEX = 8;

    public static final String GLOW_TAG_KEY = "GlowUses";
    public static final String ITEMS_TAG_KEY = "Items";

    public static List<ItemStack> getContents(ItemStack stack) {
        validateChalkBoxStack(stack);
        return getItemsListTag(stack)
                .stream()
                .map(CompoundTag.class::cast)
                .map(ItemStack::of)
                .toList();
    }

    public static boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot < 0 || slot >= SLOTS)
            return false;
        if (slot == ChalkBox.GLOWINGS_SLOT_INDEX)
            return stack.is(Chalk.Tags.Items.GLOWINGS);
        else
            return stack.getItem() instanceof ChalkItem;
    }

    /**
     * Gets the ItemStack in the ChalkBox by slot.
     * @param slot throws when slot is out of range.
     */
    public static ItemStack getItemInSlot(ItemStack chalkBoxStack, int slot) {
        validateChalkBoxStack(chalkBoxStack);
        validateSlotIndex(slot);
        return ItemStack.of(getItemsListTag(chalkBoxStack).getCompound(slot));
    }

    public static void setContents(ItemStack chalkBoxStack, List<ItemStack> items){
        Preconditions.checkArgument(items.size() <= SLOTS, "List of items cannot be larger than amount of slots available.");
        for (int index = 0; index < Math.min(SLOTS, items.size()); index++) {
            ItemStack itemStack = items.get(index);
            Preconditions.checkArgument(itemStack != null, "Stack cannot be null.");
            setSlot(chalkBoxStack, index, itemStack);
        }
    }

    public static void setSlot(ItemStack chalkBoxStack, int slot, ItemStack itemStack) {
        validateSlotIndex(slot);
        if (itemStack.isEmpty() || isItemValid(slot, itemStack))
            updateSlotContents(chalkBoxStack, slot, itemStack);
    }

    public static int getGlowLevel(ItemStack chalkBoxStack) {
        validateChalkBoxStack(chalkBoxStack);
        return chalkBoxStack.getOrCreateTag().getInt(GLOW_TAG_KEY);
    }

    public static void consumeGlow(ItemStack chalkBoxStack) {
        validateChalkBoxStack(chalkBoxStack);
        setGlow(chalkBoxStack, Math.max(getGlowLevel(chalkBoxStack) - 1, 0));
        updateGlow(chalkBoxStack);
    }

    public static void setGlow(ItemStack chalkBoxStack, int glow) {
        validateChalkBoxStack(chalkBoxStack);
        chalkBoxStack.getOrCreateTag().putInt(GLOW_TAG_KEY, glow);
    }

    private static void updateGlow(ItemStack chalkBoxStack){
        if (getGlowLevel(chalkBoxStack) > 0)
            return;

        ItemStack glowingItemStack = getItemInSlot(chalkBoxStack, GLOWINGS_SLOT_INDEX);
        if (!glowingItemStack.isEmpty()){
            setGlow(chalkBoxStack, Config.CHALK_BOX_GLOWING_USES.get());
            glowingItemStack.shrink(1);
            setSlot(chalkBoxStack, GLOWINGS_SLOT_INDEX, glowingItemStack);
        }
    }

    private static void validateChalkBoxStack(@NotNull ItemStack stack) {
        Preconditions.checkArgument(!stack.isEmpty() && stack.getItem() instanceof ChalkBoxItem);
    }

    private static void validateSlotIndex(int slot) {
        Preconditions.checkArgument( slot >= 0 && slot < SLOTS, "[%s] slot is out if range: 0-%s".formatted(slot, SLOTS - 1));
    }

    private static void updateSlotContents(ItemStack chalkBoxStack, int slot, ItemStack itemStack){
        ListTag itemsListTag = getItemsListTag(chalkBoxStack);
        itemsListTag.set(slot, itemStack.serializeNBT());

        onItemInSlotChanged(chalkBoxStack, slot, itemStack);
    }

    private static void onItemInSlotChanged(ItemStack chalkBoxStack, int slot, ItemStack itemStack) {
        if (slot == GLOWINGS_SLOT_INDEX)
            updateGlow(chalkBoxStack);
    }

    private static ListTag getItemsListTag(ItemStack chalkBoxStack){
        validateChalkBoxStack(chalkBoxStack);
        CompoundTag compoundTag = chalkBoxStack.getOrCreateTag();
        if (!compoundTag.contains(ITEMS_TAG_KEY)){
            ListTag itemTags = new ListTag();
            for (int index = 0; index < SLOTS; index++) {
                itemTags.add(ItemStack.EMPTY.serializeNBT());
            }
            compoundTag.put(ITEMS_TAG_KEY, itemTags);
        }

        return compoundTag.getList(ITEMS_TAG_KEY, ListTag.TAG_COMPOUND);
    }
}
