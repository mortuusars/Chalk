package io.github.mortuusars.chalk.items;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.setup.ModTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ChalkBox {

    public static final int SLOTS = 9;
    public static final int CHALK_SLOTS = 8;
    public static final int GLOWING_ITEM_SLOT_ID = 8;

    public static final String GLOWING_USES_TAG_KEY = "GlowUses";
    public static final String ITEMS_TAG_KEY = "Items";

    public static List<ItemStack> getContents(ItemStack stack) {
        CompoundTag compoundtag = stack.getTag();
        if (compoundtag == null)
            return Collections.emptyList();
        else {
            ListTag listtag = compoundtag.getList(ITEMS_TAG_KEY, ListTag.TAG_COMPOUND);
            return listtag.stream().map(CompoundTag.class::cast).map(ItemStack::of).toList();
        }
    }

    /**
     * Gets the ItemStack in the ChalkBox by slotID.
     * @param slotID throws when slotID is out of range.
     */
    public static ItemStack getItemInSlot(ItemStack chalkBoxStack, int slotID) {
        if (slotID >= SLOTS)
            throw new IllegalArgumentException("slotID is out if range: 0-" + (SLOTS -1) + ". Value: " + slotID);

        return ItemStack.of(getItemsListTag(chalkBoxStack).getCompound(slotID));
    }

    public static void setContents(ItemStack stack, List<ItemStack> items){
        if (items.size() > SLOTS)
            throw new IllegalArgumentException("Items count cannot be larger than amount of slots available.");

        for (int index = 0; index < Math.min(CHALK_SLOTS, items.size()); index++)
            setSlot(stack, index, items.get(index));

        if (items.size() > GLOWING_ITEM_SLOT_ID)
            setSlot(stack, GLOWING_ITEM_SLOT_ID, items.get(GLOWING_ITEM_SLOT_ID));
    }

    public static void setSlot(ItemStack chalkBoxStack, int slot, ItemStack itemStack){
        if (slot >= 0 && slot < CHALK_SLOTS){
            if (itemStack.is(ModTags.Items.CHALK) || itemStack.isEmpty())
                updateSlotContents(chalkBoxStack, slot, itemStack);
            else
                throw new IllegalArgumentException("Only '" + ModTags.Items.CHALK.location() + "' or empty ItemStack allowed in chalk slots.");
        }
        else if (slot == GLOWING_ITEM_SLOT_ID) {
            if (itemStack.is(ModTags.Items.GLOWING) || itemStack.isEmpty())
                updateSlotContents(chalkBoxStack, slot, itemStack);
            else
                throw new IllegalArgumentException("Only '" + ModTags.Items.GLOWING.location() + "' or empty ItemStack allowed in glowing item slot.");
        }
        else
            throw new IllegalArgumentException("Slot index is not in valid range - 0-" + (SLOTS - 1) + ". Value: " + slot);
    }

    private static void updateSlotContents(ItemStack chalkBoxStack, int slot, ItemStack itemStack){
        ListTag itemsListTag = getItemsListTag(chalkBoxStack);
        itemsListTag.set(slot, itemStack.serializeNBT());

        onSlotUpdated(chalkBoxStack, slot, itemStack);
    }

    private static void onSlotUpdated(ItemStack chalkBoxStack, int slot, ItemStack itemStack) {
//        Chalk.LOGGER.info("Updated slot " + slot + " with " + itemStack);
        if (slot == GLOWING_ITEM_SLOT_ID)
            updateGlowingUses(chalkBoxStack);
    }

    private static @Nullable ItemStack updateSelectedChalk(ItemStack chalkBoxStack) {
        List<ItemStack> contents = getContents(chalkBoxStack);
        for (ItemStack stack : contents) {
            if (stack.is(ModTags.Items.CHALK))
                return stack;
        }

        return null;
    }

    public static int getGlowingUses(ItemStack chalkBoxStack){
        return chalkBoxStack.getOrCreateTag().getInt(GLOWING_USES_TAG_KEY);
    }

    public static void useGlow(ItemStack chalkBoxStack) {
        int glowingUses = getGlowingUses(chalkBoxStack) - 1;
        chalkBoxStack.getOrCreateTag().putInt(GLOWING_USES_TAG_KEY, glowingUses);

        if (glowingUses <= 0)
            updateGlowingUses(chalkBoxStack);
    }

    private static void updateGlowingUses(ItemStack chalkBoxStack){
        if (getGlowingUses(chalkBoxStack) > 0)
            return;

        ItemStack glowingItemStack = getContents(chalkBoxStack).get(GLOWING_ITEM_SLOT_ID);
        if (!glowingItemStack.isEmpty()){
            chalkBoxStack.getOrCreateTag().putInt(GLOWING_USES_TAG_KEY, CommonConfig.CHALK_BOX_GLOWING_USES.get());
            glowingItemStack.shrink(1);
            setSlot(chalkBoxStack, GLOWING_ITEM_SLOT_ID, glowingItemStack);
        }
    }

    private static ListTag getItemsListTag(ItemStack chalkBoxStack){
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
