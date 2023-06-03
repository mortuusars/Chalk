package io.github.mortuusars.chalk.items;

import com.google.common.base.Preconditions;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.core.IDrawingTool;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.data.Lang;
import io.github.mortuusars.chalk.menus.ChalkBoxItemStackHandler;
import io.github.mortuusars.chalk.menus.ChalkBoxMenu;
import io.github.mortuusars.chalk.network.Packets;
import io.github.mortuusars.chalk.network.packet.ServerboundOpenChalkBoxPacket;
import io.github.mortuusars.chalk.render.ChalkColors;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChalkBoxItem extends Item implements IDrawingTool {
    public static final ResourceLocation SELECTED_PROPERTY = Chalk.resource("selected");

    public ChalkBoxItem(Properties properties) {
        super(properties.setNoRepair());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        int selectedChalkIndex = getSelectedChalkIndex(stack);

        // Drawing with: [chalk]
        if (selectedChalkIndex != -1) {
            ItemStack selectedChalk = ChalkBox.getItemInSlot(stack, selectedChalkIndex);
            Style style = selectedChalk.getItem() instanceof ChalkItem chalkItem ?
                    Style.EMPTY.withColor(ChalkColors.fromDyeColor(chalkItem.getColor()))
                    : Style.EMPTY.withColor(ChatFormatting.WHITE);

            tooltipComponents.add(Lang.CHALK_BOX_DRAWING_WITH_TOOLTIP.translate()
                    .withStyle(ChatFormatting.GRAY)
                    .append(((MutableComponent) selectedChalk.getHoverName())
                            .withStyle(style)));
        }

        // <Right Click to open>
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {

            if (screen instanceof CreativeModeInventoryScreen creativeScreen
                    && creativeScreen.getSelectedTab() != CreativeModeTab.TAB_INVENTORY.getId())
                return; // Cannot open while in other tabs than inventory.

            Slot slotUnderMouse = screen.getSlotUnderMouse();
            if (slotUnderMouse != null && slotUnderMouse.container instanceof Inventory) {
                tooltipComponents.add(Lang.CHALK_BOX_OPEN_TOOLTIP.translate()
                                .withStyle(ChatFormatting.ITALIC)
                                .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack otherStack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        // Open
        if (stack.getItem() == this && otherStack.isEmpty() && action == ClickAction.SECONDARY && slot.container instanceof Inventory) {
            if (player.level.isClientSide) {
                if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen creativeScreen
                        && creativeScreen.getSelectedTab() != CreativeModeTab.TAB_INVENTORY.getId()) {
                    // There's some problems with opening Chalk Box while in the creative tabs other than inventory.
                    // IDK how to fix it.
                    return false;
                }
                else
                    Packets.sendToServer(new ServerboundOpenChalkBoxPacket(slot.getContainerSlot()));
            }

            return true;
        }

        // Insert chalk into box:
        if (stack.getItem() == this && otherStack.getItem() instanceof ChalkItem && action == ClickAction.SECONDARY) {
            for (int i = 0; i < ChalkBox.CHALK_SLOTS; i++) {
                if (ChalkBox.getItemInSlot(stack, i).isEmpty()) {
                    ChalkBox.setSlot(stack, i, otherStack);
                    player.playSound(Chalk.SoundEvents.CHALK_BOX_CHANGE.get(),
                            0.9f, 0.9f + player.level.random.nextFloat() * 0.2f);
                    otherStack.setCount(0);
                    return true; // Handled
                }
            }
        }

        return false;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        ItemStack chalkBox = context.getItemInHand();
        if (!chalkBox.is(this))
            return InteractionResult.FAIL;

        Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.FAIL;

        if (context.getHand() == InteractionHand.OFF_HAND && (player.getMainHandItem().getItem() instanceof ChalkItem || player.getMainHandItem().is(this)) )
            return InteractionResult.FAIL; // Skip drawing from offhand if chalks in both hands.

        int selectedChalkIndex = getSelectedChalkIndex(chalkBox);
        if (selectedChalkIndex == -1) {
            openGUI(player, chalkBox);
            return InteractionResult.SUCCESS;
        }

        MarkDrawingContext drawingContext = createDrawingContext(context);

        if (!drawingContext.canDraw())
            return InteractionResult.FAIL;

        ItemStack selectedChalk = ChalkBox.getItemInSlot(chalkBox, selectedChalkIndex);

        if (player.isSecondaryUseActive()) {
            drawingContext.openSymbolSelectionScreen();
            return InteractionResult.CONSUME;
        }

        if (drawMark(drawingContext, drawingContext.createRegularMark(((ChalkItem) selectedChalk.getItem()).getColor(),
                ChalkBox.getGlowLevel(chalkBox) > 0)))
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);

        return InteractionResult.FAIL;
    }

    // Called when not looking at a block
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack usedStack = player.getItemInHand(usedHand);

        if (!usedStack.is(this))
            return InteractionResultHolder.pass(usedStack);

        if (player.isSecondaryUseActive()) {
            changeSelectedChalk(usedStack);
            level.playSound(player, player.position().x, player.position().y, player.position().z, Chalk.SoundEvents.CHALK_BOX_CHANGE.get(), SoundSource.PLAYERS,
                    0.9f, 0.9f + level.random.nextFloat() * 0.2f);
        }
        else
            openGUI(player, usedStack);

        return InteractionResultHolder.sidedSuccess(usedStack, level.isClientSide);
    }

    public static void openGUI(Player player, ItemStack usedStack) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider( (containerID, playerInventory, playerEntity) ->
                            new ChalkBoxMenu(containerID, playerInventory, usedStack, new ChalkBoxItemStackHandler(usedStack)),
                            usedStack.getHoverName()), buffer -> buffer.writeItem(usedStack.copy()));
            player.level.playSound(null, player.position().x, player.position().y, player.position().z,
                    Chalk.SoundEvents.CHALK_BOX_OPEN.get(), SoundSource.PLAYERS,
                    0.9f, 0.9f + player.level.random.nextFloat() * 0.2f);
        }
    }

    /**
     * Shifts chalks inside until first slot is changed to chalk with other color.
     */
    private void changeSelectedChalk(ItemStack chalkBox) {
        Preconditions.checkArgument(chalkBox.getItem() instanceof ChalkBoxItem, "Item was not a Chalk Box.");

        List<ItemStack> stacks = new ArrayList<>(ChalkBox.CHALK_SLOTS);
        int chalks = 0;
        for (int slot = 0; slot < ChalkBox.CHALK_SLOTS; slot++) {
            ItemStack slotStack = ChalkBox.getItemInSlot(chalkBox, slot);
            stacks.add(slotStack);
            if (!slotStack.isEmpty() && slotStack.getItem() instanceof ChalkItem)
                chalks++;
        }

        if (chalks > 1) {
            int selectedChalkIndex = getSelectedChalkIndex(chalkBox);
            ItemStack selectedChalk = ChalkBox.getItemInSlot(chalkBox, selectedChalkIndex);
            DyeColor selectedColor = ((ChalkItem)selectedChalk.getItem()).getColor();
            ItemStack firstStack = stacks.get(0);

            for (int i = 0; i < ChalkBox.CHALK_SLOTS; i++) {
                ItemStack stack = stacks.get(0);
                stacks.remove(stack);
                stacks.add(stack);

                stack = stacks.get(0);

                if (stack.getItem() instanceof ChalkItem chalkItem && !stack.equals(firstStack, false)
                        && !chalkItem.getColor().equals(selectedColor)) {
                    break;
                }
            }

            ChalkBox.setContents(chalkBox, stacks);
        }
    }

    private int getSelectedChalkIndex(ItemStack chalkBoxStack) {
        for (int slot = 0; slot < ChalkBox.CHALK_SLOTS; slot++) {
            ItemStack itemInSlot = ChalkBox.getItemInSlot(chalkBoxStack, slot);
            if (itemInSlot.getItem() instanceof ChalkItem) {
                return slot;
            }
        }

        return -1;
    }

    // Used by ItemOverrides to determine what chalk to display with the item texture.
    public float getSelectedChalkColor(ItemStack stack){
        if (stack.hasTag()) {
            for (int i = 0; i < ChalkBox.CHALK_SLOTS; i++) {
                if (ChalkBox.getItemInSlot(stack, i).getItem() instanceof ChalkItem chalkItem)
                    return chalkItem.getColor().getId() + 1;
            }
        }

        return 0f;
    }

    @Override
    public void onMarkDrawn(Player player, InteractionHand hand, BlockPos markBlockPos, Mark mark) {
        if (player.isCreative())
            return;



        ItemStack chalkBox = player.getItemInHand(hand);

        Preconditions.checkArgument(chalkBox.getItem() instanceof ChalkBoxItem, "ChalkBox expected in player's hand.");

        int selectedChalkIndex = getSelectedChalkIndex(chalkBox);
        ItemStack selectedChalk = ChalkBox.getItemInSlot(chalkBox, selectedChalkIndex);
        ItemStack resultChalk = ChalkItem.damageAndDestroy(selectedChalk, player);

        ChalkBox.setSlot(chalkBox, selectedChalkIndex, resultChalk);

        if (mark.glowing())
            ChalkBox.consumeGlow(chalkBox);
    }

    @Override
    public Optional<DyeColor> getMarkColor(ItemStack chalkBoxStack) {
        Preconditions.checkArgument(chalkBoxStack.getItem() instanceof ChalkBoxItem, "ChalkBox expected in player's hand.");
        int selectedChalkIndex = getSelectedChalkIndex(chalkBoxStack);

        if (selectedChalkIndex == -1)
            return Optional.empty();

        ItemStack selectedChalk = ChalkBox.getItemInSlot(chalkBoxStack, selectedChalkIndex);
        return selectedChalk.getItem() instanceof IDrawingTool drawingTool ? drawingTool.getMarkColor(selectedChalk) : Optional.empty();
    }

    @Override
    public boolean getGlowing(ItemStack chalkBoxStack) {
        Preconditions.checkArgument(chalkBoxStack.getItem() instanceof ChalkBoxItem, "ChalkBox expected in player's hand.");
        return ChalkBox.getGlowLevel(chalkBoxStack) > 0;
    }

    @Override
    public boolean isRepairable(@NotNull ItemStack stack) {
        return false;
    }
    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }
}
