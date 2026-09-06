package io.github.mortuusars.chalk.world.item;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.world.chalk.MarkDrawingContext;
import io.github.mortuusars.chalk.world.inventory.ChalkBoxMenu;
import io.github.mortuusars.chalk.world.item.component.ChalkBoxContents;
import io.github.mortuusars.mortaar.Platform;
import io.github.mortuusars.mortaar.world.item.ApplicationTargetItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ChalkBoxItem extends Item implements MarkDrawable, ApplicationTargetItem {
    public ChalkBoxItem(Properties properties) {
        super(properties);
    }

    public @NotNull ChalkBoxContents getContents(ItemStack stack) {
        return ChalkBoxContents.of(stack);
    }

    public ItemStack getSelectedChalk(ItemStack stack) {
        return getContents(stack).getSelectedChalk();
    }

    public int getTintColor(ItemStack stack, int index) {
        if (index != 1) {
            return 0xFFFFFFFF;
        }

        ItemStack selectedChalk = getSelectedChalk(stack);
        if (selectedChalk.isEmpty() || !(selectedChalk.getItem() instanceof ChalkItem chalkItem)) {
            return 0x00000000;
        }

        return chalkItem.getTintColor(selectedChalk, 0);
    }

    // -- Tooltip

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        return Config.Client.CHALK_BOX_TOOLTIP_CONTENTS.get() && !getContents(stack).isEmpty()
              ? Optional.of(getContents(stack)) : Optional.empty();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        if (!Config.Client.CHALK_BOX_TOOLTIP_DETAILS.get()) {
            return;
        }

        if (!Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("item.chalk.tooltip.hold_for_details"));
        } else {
            tooltipComponents.add(Component.translatable("item.chalk.chalk_box.tooltip.open"));
            tooltipComponents.add(Component.translatable("item.chalk.chalk_box.tooltip.insert"));
            tooltipComponents.add(Component.translatable("item.chalk.chalk_box.tooltip.remove"));
            tooltipComponents.add(Component.translatable("item.chalk.chalk_box.tooltip.change_selected"));
        }
    }

    @Override
    public boolean shouldRenderSlotTooltipWhileCarrying(Player player, AbstractContainerMenu menu, Slot slot, ItemStack carried) {
        return ChalkBoxContents.canHold(carried);
    }

    // -- Bar

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return Config.Server.CHALK_BOX_SHOW_DURABILITY_BAR.get()
              ? getSelectedChalk(stack).isBarVisible()
              : super.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return getSelectedChalk(stack).getBarWidth();
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return getSelectedChalk(stack).getBarColor();
    }

    // --

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }

        ChalkBoxContents contents = getContents(stack);

        // Extract selected
        if (other.isEmpty()) {
            ItemStack selectedChalk = contents.getSelectedChalk();
            if (!selectedChalk.isEmpty()) {
                slot.set(contents.mutable().setItem(contents.selected(), ItemStack.EMPTY).toImmutable(stack));
                access.set(selectedChalk.copy());
                player.level().playSound(player, player, Chalk.SoundEvents.CHALK_BOX_CHANGE.get(), SoundSource.PLAYERS, 1, 1);
                player.resetAttackStrengthTicker();
                return true;
            }

            ItemStack glowingItem = contents.getItem(ChalkBoxContents.GLOWINGS_SLOT);
            if (Config.Server.CHALK_BOX_GLOWING_ENABLED.get() && !glowingItem.isEmpty()) {
                slot.set(contents.mutable().setItem(ChalkBoxContents.GLOWINGS_SLOT, ItemStack.EMPTY).toImmutable(stack));
                access.set(glowingItem.copy());
                player.level().playSound(player, player, Chalk.SoundEvents.CHALK_BOX_CHANGE.get(), SoundSource.PLAYERS, 1, 1);
                player.resetAttackStrengthTicker();
                return true;
            }

            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.6f, 1f);
            return true;
        }

        // Insert chalk
        if (other.getItem() instanceof ChalkItem) {
            for (int i = 0; i < ChalkBoxContents.CHALK_SLOTS; i++) {
                if (contents.getItem(i).isEmpty()) {
                    access.set(ItemStack.EMPTY);
                    slot.set(contents.mutable().setItem(i, other.copy()).toImmutable(stack));
                    player.level().playSound(player, player, Chalk.SoundEvents.CHALK_BOX_CHANGE.get(), SoundSource.PLAYERS, 1, 1);
                    player.resetAttackStrengthTicker();
                    return true;
                }
            }

            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.6f, 1f);
            return true;
        }

        // Insert glowing
        if (Config.Server.GLOWING_ENABLED.get() && Config.Server.CHALK_BOX_GLOWING_ENABLED.get() && other.is(Chalk.Tags.Items.GLOWINGS)) {
            ItemStack existing = contents.getItem(ChalkBoxContents.GLOWINGS_SLOT);
            int glowBeforeInsertion = contents.glow();

            if (existing.isEmpty()) {
                access.set(ItemStack.EMPTY);
                slot.set(contents.mutable()
                      .setItem(ChalkBoxContents.GLOWINGS_SLOT, other.copy())
                      .updateGlow()
                      .toImmutable(stack));
            } else if (existing.getCount() >= existing.getMaxStackSize() || !ItemStack.isSameItemSameComponents(existing, other)) {
                player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.6f, 1f);
                return true;
            } else {
                int insertedAmount = Math.min(other.getCount(), existing.getMaxStackSize() - existing.getCount());
                if (insertedAmount <= 0) {
                    return true;
                }

                existing = existing.copy();
                existing.setCount(existing.getCount() + insertedAmount);
                slot.set(contents.mutable()
                      .setItem(ChalkBoxContents.GLOWINGS_SLOT, existing)
                      .updateGlow()
                      .toImmutable(stack));

                other.split(insertedAmount);
                access.set(other);
            }

            player.level().playSound(player, player, Chalk.SoundEvents.CHALK_BOX_CHANGE.get(), SoundSource.PLAYERS, 1, 1);
            player.resetAttackStrengthTicker();

            if (glowBeforeInsertion < getContents(slot.getItem()).glow()) {
                player.playSound(Chalk.SoundEvents.GLOW_APPLIED.get());
                player.playSound(Chalk.SoundEvents.GLOWING.get());
            }

            return true;
        }

        player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.6f, 1f);
        return true;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        InteractionHand hand = context.getHand();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        ItemStack selectedChalk = getSelectedChalk(stack);

        if (selectedChalk.isEmpty()) {
            if (player instanceof ServerPlayer serverPlayer) {
                open(serverPlayer, hand);
            }
            player.resetAttackStrengthTicker();
            return InteractionResult.SUCCESS;
        }

        convertOldChalks(player, hand, stack);

        if (!(selectedChalk.getItem() instanceof MarkDrawable)) {
            Chalk.LOGGER.error("Cannot draw mark using a Chalk Box: selected chalk is not a drawable item, but {}.", selectedChalk.getItem());
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.6f, 1f);
            return InteractionResult.FAIL;
        }

        MarkDrawingContext drawingContext = createMarkDrawingContext(player, context.getHand(),
              context.getClickLocation(), context.getClickedPos(), context.getClickedFace());

        if (!canDrawMark(player, drawingContext)) {
            return InteractionResult.FAIL;
        }

        if (player.isSecondaryUseActive()) {
            selectSymbolAndDraw(player, drawingContext);
            return InteractionResult.CONSUME;
        }

        if (drawMark(player, drawingContext, createRegularMark(player, drawingContext, stack))) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }

        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isSecondaryUseActive()) {
            convertOldChalks(player, hand, stack);

            ChalkBoxContents contents = getContents(stack);
            if (contents.getChalkCount() <= 1) {
                player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.6f, 1f);
                return InteractionResultHolder.fail(stack);
            }

            int newSelected = contents.selected() + 1;
            if (newSelected >= ChalkBoxContents.CHALK_SLOTS) {
                newSelected = 0;
            }
            contents.mutable()
                  .setSelected(newSelected)
                  .toImmutable(stack);

            player.level().playSound(player, player, Chalk.SoundEvents.CHALK_BOX_CHANGE.get(), SoundSource.PLAYERS, 1, 1);
            player.resetAttackStrengthTicker();

            return InteractionResultHolder.consume(stack);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            open(serverPlayer, hand);
        }
        player.resetAttackStrengthTicker();
        return InteractionResultHolder.consume(stack);
    }

    public void open(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!(stack.getItem() instanceof ChalkBoxItem)) {
            Chalk.LOGGER.error("Cannot open Chalk Box menu: {} is not a ChalkBoxItem.", stack);
            return;
        }

        convertOldChalks(player, hand, stack);

        Component title = stack.has(DataComponents.CUSTOM_NAME)
              ? stack.getHoverName()
              : Component.translatable("container.chalk.chalk_box");

        SimpleMenuProvider menuProvider = new SimpleMenuProvider((containerID, playerInventory, playerEntity) ->
              new ChalkBoxMenu(containerID, playerInventory, hand), title);

        Platform.openMenu(player, menuProvider, buffer -> buffer.writeEnum(hand));

        player.level().playSound(null, player.position().x, player.position().y, player.position().z,
              Chalk.SoundEvents.CHALK_BOX_OPEN.get(), SoundSource.PLAYERS,
              0.9f, 0.9f + player.level().random.nextFloat() * 0.2f);
    }

    @SuppressWarnings("removal")
    protected void convertOldChalks(Player player, InteractionHand hand, ItemStack stack) {
        ChalkBoxContents contents = getContents(stack);
        if (contents.items().stream().anyMatch(s -> s.getItem() instanceof OldChalkItem)) {
            List<ItemStack> items = contents.copyItems().stream()
                  .map(s -> {
                      if (s.getItem() instanceof OldChalkItem oldChalkItem) {
                          return oldChalkItem.convert(s);
                      }
                      return s;
                  })
                  .toList();
            player.setItemInHand(hand, contents.mutable().setItems(items).toImmutable(stack));
        }
    }

    // -- Drawable

    @Override
    public int getMarkDrawingColor(ItemStack chalkBoxStack) {
        ItemStack chalk = getSelectedChalk(chalkBoxStack);
        return chalk.getItem() instanceof MarkDrawable drawable
              ? drawable.getMarkDrawingColor(chalk)
              : 0xFFFFFFFF;
    }

    @Override
    public boolean shouldDrawGlowingMark(ItemStack chalkBoxStack) {
        return Config.Server.CHALK_BOX_GLOWING_ENABLED.get() && getContents(chalkBoxStack).glow() > 0;
    }

    @Override
    public void onMarkDrawn(Player player, MarkDrawingContext context, Mark mark) {
        MarkDrawable.super.onMarkDrawn(player, context, mark);

        ItemStack chalkBoxStack = player.getItemInHand(context.hand());
        ChalkBoxContents contents = getContents(chalkBoxStack);
        int selectedChalkIndex = contents.selected();
        if (selectedChalkIndex < 0) {
            Chalk.LOGGER.error("Failed to apply Chalk Box onMarkDrawn side effects: no selected chalk. {}", chalkBoxStack.getComponents());
            return;
        }

        ItemStack selectedChalk = contents.getItem(selectedChalkIndex).copy();
        if (selectedChalk.getItem() instanceof ChalkItem chalk) {
            chalk.onChalkMarkDrawn(player, context.hand(), selectedChalk, context.markPos(), context.markFacing(), mark);
        }

        if (contents.glow() > 0 && player instanceof ServerPlayer serverPlayer) {
            BlockPos surfacePos = context.surfacePos();
            Chalk.CriteriaTriggers.MARK_GLOWING.get().trigger(
                  serverPlayer, mark, context.markPos(), player.level().getBlockState(surfacePos).getMapColor(player.level(), surfacePos));
        }

        if (!player.isCreative()) {
            selectedChalk.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.hand()));

            player.setItemInHand(context.hand(), contents.mutable()
                  .setItem(selectedChalkIndex, selectedChalk)
                  .consumeGlow()
                  .toImmutable(chalkBoxStack));
        }
    }
}
