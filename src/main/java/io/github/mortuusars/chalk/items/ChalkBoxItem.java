package io.github.mortuusars.chalk.items;

import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.blocks.MarkSymbol;
import io.github.mortuusars.chalk.core.ChalkMark;
import io.github.mortuusars.chalk.data.Lang;
import io.github.mortuusars.chalk.menus.ChalkBoxItemStackHandler;
import io.github.mortuusars.chalk.menus.ChalkBoxMenu;
import io.github.mortuusars.chalk.render.ChalkColors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChalkBoxItem extends Item {
    public static final ResourceLocation SELECTED_PROPERTY = Chalk.resource("selected");

    public ChalkBoxItem(Properties properties) {
        super(properties.setNoRepair());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        Pair<ItemStack, Integer> firstChalkStack = getFirstChalkStack(stack);

        if (firstChalkStack != null) {
            Style style = firstChalkStack.getFirst().getItem() instanceof ChalkItem chalkItem ?
                    Style.EMPTY.withColor(ChalkColors.fromDyeColor(chalkItem.getColor()))
                    : Style.EMPTY.withColor(ChatFormatting.WHITE);

            tooltipComponents.add(Lang.CHALK_BOX_DRAWING_WITH_TOOLTIP.translate()
                    .withStyle(ChatFormatting.GRAY)
                    .append(((MutableComponent) firstChalkStack.getFirst().getHoverName())
                            .withStyle(style)));
        }

        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isCreative()) {
            tooltipComponents.add(Lang.CHALK_BOX_OPEN_TOOLTIP.translate()
                            .withStyle(ChatFormatting.ITALIC)
                            .withStyle(Style.EMPTY.withColor(0x888888)));
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, @NotNull ItemStack otherStack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        if (!player.isCreative() && stack.getItem() == this
                && otherStack.isEmpty() && action == ClickAction.SECONDARY) {
            openGUI(player, stack);
            player.playSound(Chalk.SoundEvents.CHALK_BOX_OPEN.get(),
                    0.9f, 0.9f + player.level.random.nextFloat() * 0.2f);
            return true;
        }

        if (stack.getItem() == this && otherStack.getItem() instanceof ChalkItem && action == ClickAction.SECONDARY) {
            for (int i = 0; i < ChalkBox.CHALK_SLOTS; i++) {
                if (ChalkBox.getItemInSlot(stack, i).isEmpty()) {
                    ChalkBox.setSlot(stack, i, otherStack);
                    player.playSound(Chalk.SoundEvents.CHALK_BOX_CHANGE.get(),
                            0.9f, 0.9f + player.level.random.nextFloat() * 0.2f);
                    otherStack.setCount(0);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        // Mark will be drawn even if block can be activated (if shift is held)
        if (context.getPlayer() != null && context.getPlayer().isSecondaryUseActive())
            return useOn(context);

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        ItemStack chalkBoxStack = context.getItemInHand();
        if (!chalkBoxStack.is(this))
            return InteractionResult.FAIL;

        Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.PASS;

        if (context.getHand() == InteractionHand.OFF_HAND && (player.getMainHandItem().getItem() instanceof ChalkItem || player.getMainHandItem().is(this)) )
            return InteractionResult.FAIL; // Skip drawing from offhand if chalks in both hands.

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        Pair<ItemStack, Integer> chalkStack = getFirstChalkStack(chalkBoxStack);

        if ( chalkStack == null || !ChalkMark.canBeDrawnAt(clickedPos.relative(clickedFace), clickedPos, clickedFace, level) )
            return InteractionResult.FAIL;

        MarkSymbol symbol = context.isSecondaryUseActive() ? MarkSymbol.CROSS : MarkSymbol.NONE;
        DyeColor chalkColor = ((ChalkItem) chalkStack.getFirst().getItem()).getColor();
        final boolean isGlowing = ChalkBox.getGlow(chalkBoxStack) > 0;

        if (ChalkMark.draw(symbol, chalkColor, isGlowing, clickedPos, clickedFace, context.getClickLocation(), level) == InteractionResult.SUCCESS) {
            if ( !player.isCreative() ) {
                ItemStack chalkItemStack = chalkStack.getFirst();

                if (chalkItemStack.isDamageableItem()) {
                    chalkItemStack.setDamageValue(chalkItemStack.getDamageValue() + 1);
                    if (chalkItemStack.getDamageValue() >= chalkItemStack.getMaxDamage()){
                        chalkItemStack = ItemStack.EMPTY;
                        Vec3 playerPos = player.position();
                        level.playSound(player, playerPos.x, playerPos.y, playerPos.z, Chalk.SoundEvents.CHALK_BROKEN.get(),
                                SoundSource.PLAYERS, 0.9f, 0.9f + level.random.nextFloat() * 0.2f);
                    }
                }

                ChalkBox.setSlot(chalkBoxStack, chalkStack.getSecond(), chalkItemStack);

                if (isGlowing)
                    ChalkBox.consumeGlow(chalkBoxStack);
            }

            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

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
        else {
            openGUI(player, usedStack);
            level.playSound(player, player.position().x, player.position().y, player.position().z, Chalk.SoundEvents.CHALK_BOX_OPEN.get(), SoundSource.PLAYERS,
                    0.9f, 0.9f + level.random.nextFloat() * 0.2f);
        }

        return InteractionResultHolder.sidedSuccess(usedStack, level.isClientSide);
    }

    private void openGUI(Player player, ItemStack usedStack) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider( (containerID, playerInventory, playerEntity) ->
                            new ChalkBoxMenu(containerID, playerInventory, usedStack, new ChalkBoxItemStackHandler(usedStack)),
                            usedStack.getHoverName()), buffer -> buffer.writeItem(usedStack.copy()));
        }
    }

    /**
     * Shifts stacks until first slot is changed to another chalk.
     */
    private void changeSelectedChalk(ItemStack usedStack) {
        List<ItemStack> stacks = new ArrayList<>(16);
        int chalks = 0;
        for (int slot = 0; slot < ChalkBox.CHALK_SLOTS; slot++) {
            ItemStack slotStack = ChalkBox.getItemInSlot(usedStack, slot);
            stacks.add(slotStack);
            if (!slotStack.isEmpty())
                chalks++;
        }

        if (chalks >= 2) {
            DyeColor selectedColor = ((ChalkItem) Objects.requireNonNull(getFirstChalkStack(usedStack)).getFirst().getItem()).getColor();
            ItemStack firstStack = stacks.get(0);

            for (int i = 0; i < 8; i++) {
                ItemStack stack = stacks.get(0);
                stacks.remove(stack);
                stacks.add(stack);

                stack = stacks.get(0);

                if (stack.getItem() instanceof ChalkItem chalkItem && !stack.equals(firstStack, false)
                        && !chalkItem.getColor().equals(selectedColor)) {
                    break;
                }
            }

            ChalkBox.setContents(usedStack, stacks);
        }
    }

    private @Nullable Pair<ItemStack, Integer> getFirstChalkStack(ItemStack chalkBoxStack) {
        for (int slot = 0; slot < ChalkBox.CHALK_SLOTS; slot++) {
            ItemStack itemInSlot = ChalkBox.getItemInSlot(chalkBoxStack, slot);
            if (itemInSlot.getItem() instanceof ChalkItem) {
                return Pair.of(itemInSlot, slot);
            }
        }

        return null;
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
