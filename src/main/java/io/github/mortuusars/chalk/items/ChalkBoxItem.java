package io.github.mortuusars.chalk.items;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.blocks.MarkSymbol;
import io.github.mortuusars.chalk.core.ChalkMark;
import io.github.mortuusars.chalk.menus.ChalkBoxItemStackHandler;
import io.github.mortuusars.chalk.menus.ChalkBoxMenu;
import io.github.mortuusars.chalk.setup.ModItems;
import io.github.mortuusars.chalk.setup.ModSounds;
import io.github.mortuusars.chalk.setup.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChalkBoxItem extends Item {

    public ChalkBoxItem(Properties properties) {
        super(properties.setNoRepair());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        Pair<ItemStack, Integer> firstChalkStack = getFirstChalkStack(pStack);
        if (firstChalkStack != null) {
            pTooltipComponents.add(new TranslatableComponent("item.chalk.chalk_box.tooltip.drawing_with").withStyle(ChatFormatting.GRAY)
                    .append(((BaseComponent) firstChalkStack.getFirst().getHoverName()).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.WHITE)));
        }
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

        if (context.getHand() == InteractionHand.OFF_HAND && (player.getMainHandItem().is(ModTags.Items.CHALK) || player.getMainHandItem().is(this)) )
            return InteractionResult.FAIL; // Skip drawing from offhand if chalks in both hands.

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        Pair<ItemStack, Integer> chalkStack = getFirstChalkStack(chalkBoxStack);

        if ( chalkStack == null || !ChalkMark.canBeDrawnAt(clickedPos.relative(clickedFace), clickedPos, clickedFace, level) )
            return InteractionResult.FAIL;

        MarkSymbol symbol = context.isSecondaryUseActive() ? MarkSymbol.CROSS : MarkSymbol.NONE;
        DyeColor chalkColor = ((ChalkItem) chalkStack.getFirst().getItem()).getColor();
        final boolean isGlowing = ChalkBox.getGlowingUses(chalkBoxStack) > 0;

        if (ChalkMark.draw(symbol, chalkColor, isGlowing, clickedPos, clickedFace, context.getClickLocation(), level) == InteractionResult.SUCCESS) {
            if ( !player.isCreative() ) {
                ItemStack chalkItemStack = chalkStack.getFirst();

                if (chalkItemStack.isDamageableItem()) {
                    chalkItemStack.setDamageValue(chalkItemStack.getDamageValue() + 1);
                    if (chalkItemStack.getDamageValue() >= chalkItemStack.getMaxDamage()){
                        chalkItemStack = ItemStack.EMPTY;
                        Vec3 playerPos = player.position();
                        level.playSound(player, playerPos.x, playerPos.y, playerPos.z, ModSounds.CHALK_BROKEN.get(),
                                SoundSource.BLOCKS, 0.9f, 0.9f + level.random.nextFloat() * 0.2f);
                    }
                }

                ChalkBox.setSlot(chalkBoxStack, chalkStack.getSecond(), chalkItemStack);

                if (isGlowing)
                    ChalkBox.useGlow(chalkBoxStack);
            }

            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        return InteractionResult.FAIL;
    }

    // Called when not looking at a block
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack usedStack = player.getItemInHand(usedHand);

        if (!usedStack.is(this) || !(player instanceof ServerPlayer))
            return InteractionResultHolder.pass(usedStack);

        if (player.isSecondaryUseActive()) {
            changeSelectedChalk(usedStack);
        }
        else {
            NetworkHooks.openGui((ServerPlayer) player,
                    new SimpleMenuProvider( (containerID, playerInventory, playerEntity) ->
                            new ChalkBoxMenu(containerID, playerInventory, usedStack, new ChalkBoxItemStackHandler(usedStack)),
                            usedStack.getHoverName()), buffer -> buffer.writeItem(usedStack.copy()));
        }

        return InteractionResultHolder.sidedSuccess(usedStack, level.isClientSide);
    }

    /**
     * Shifts stacks until first slot is changed to another chalk.
     */
    private static void changeSelectedChalk(ItemStack usedStack) {
        List<ItemStack> stacks = new ArrayList<>(16);
        int chalks = 0;
        for (int slot = 0; slot < ChalkBox.CHALK_SLOTS; slot++) {
            ItemStack slotStack = ChalkBox.getItemInSlot(usedStack, slot);
            stacks.add(slotStack);
            if (!slotStack.isEmpty())
                chalks++;
        }

        if (chalks > 1) {
            ItemStack firstStack = stacks.get(0);

            for (int i = 0; i < 8; i++) {
                ItemStack lastStack = stacks.get(stacks.size() - 1);
                stacks.remove(lastStack);
                stacks.add(0, lastStack);
                if (!lastStack.isEmpty() && !lastStack.equals(firstStack, false))
                    break;
            }

            ChalkBox.setContents(usedStack, stacks);
        }
    }

    private @Nullable Pair<ItemStack, Integer> getFirstChalkStack(ItemStack chalkBoxStack) {
        for (int slot = 0; slot < ChalkBox.CHALK_SLOTS; slot++) {
            ItemStack itemInSlot = ChalkBox.getItemInSlot(chalkBoxStack, slot);
            if (itemInSlot.is(ModTags.Items.CHALK)) {
                return Pair.of(itemInSlot, slot);
            }
        }

        return null;
    }

    // Used by ItemOverrides to determine what chalk to display with the item texture.
    public float getSelectedChalkColor(ItemStack stack){
        if (stack.hasTag()) {
            for (int i = 0; i < ChalkBox.CHALK_SLOTS; i++) {
                ItemStack chalkStack = ChalkBox.getItemInSlot(stack, i);
                if (!chalkStack.isEmpty())
                    return ((ChalkItem) chalkStack.getItem()).getColor().getId() + 1;
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
