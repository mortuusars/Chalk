package io.github.mortuusars.chalk.items;

import io.github.mortuusars.chalk.blocks.MarkSymbol;
import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.core.ChalkMark;
import io.github.mortuusars.chalk.setup.ModSoundEvents;
import io.github.mortuusars.chalk.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ChalkItem extends Item {

    private final DyeColor color;

    public ChalkItem(DyeColor dyeColor, Properties properties) {
        super(properties
                .tab(CreativeModeTab.TAB_TOOLS)
                .stacksTo(1)
                .defaultDurability(64)
                .setNoRepair());

        color = dyeColor;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return CommonConfig.CHALK_DURABILITY.get();
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isSecondaryUseActive())
            return useOn(context);

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        final InteractionHand hand = context.getHand();
        final ItemStack itemStack = context.getItemInHand();
        final Player player = context.getPlayer();

        if (player == null || !(itemStack.getItem() instanceof ChalkItem))
            return InteractionResult.FAIL;

        // When holding chalks in both hands - skip drawing from offhand
        if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof ChalkItem)
            return InteractionResult.FAIL;

        final Level level = context.getLevel();
        final BlockPos clickedPos = context.getClickedPos();
        final Direction clickedFace = context.getClickedFace();
        final boolean isGlowing = player.getOffhandItem().is(ModTags.Items.GLOWING);

        MarkSymbol symbol = context.isSecondaryUseActive() ? MarkSymbol.CROSS : MarkSymbol.NONE;

        if (ChalkMark.draw(symbol, color, isGlowing, clickedPos, clickedFace, context.getClickLocation(), level) == InteractionResult.SUCCESS) {
            if (!player.isCreative())
                damageAndConsumeItems(hand, itemStack, player, level, isGlowing);

            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        return InteractionResult.FAIL;
    }

    private void damageAndConsumeItems(InteractionHand hand, ItemStack itemStack, Player player, Level level, boolean isGlowing) {
        if (!itemStack.isDamageableItem())
            return;

        itemStack.setDamageValue(itemStack.getDamageValue() + 1);
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
            Vec3 playerPos = player.position();
            level.playSound(player, playerPos.x, playerPos.y, playerPos.z, ModSoundEvents.CHALK_BROKEN.get(),
                    SoundSource.PLAYERS, 0.9f, 0.9f + level.random.nextFloat() * 0.2f);
        }

        if (isGlowing)
            player.getOffhandItem().shrink(1);
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
