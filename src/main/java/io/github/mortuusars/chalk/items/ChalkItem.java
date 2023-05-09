package io.github.mortuusars.chalk.items;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.core.IDrawingTool;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ChalkItem extends Item implements IDrawingTool {
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
    public @NotNull InteractionResult useOn(UseOnContext context) {
        final InteractionHand hand = context.getHand();
        final ItemStack itemStack = context.getItemInHand();
        final Player player = context.getPlayer();

        if (player == null || !(itemStack.getItem() instanceof ChalkItem))
            return InteractionResult.FAIL;

        // When holding chalks in both hands - skip drawing from offhand
        if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof ChalkItem)
            return InteractionResult.FAIL;

        MarkDrawingContext drawingContext = createDrawingContext(player, context.getClickedPos(), context.getClickLocation(), context.getClickedFace(), hand);

        if (!drawingContext.canDraw())
            return InteractionResult.FAIL;

        if (player.isSecondaryUseActive()) {
            drawingContext.openSymbolSelectionScreen();
            return InteractionResult.CONSUME;
        }

        if (drawRegularMark(drawingContext, color, false))
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        else if (drawingContext.hasExistingMark())
            return InteractionResult.PASS;

        return InteractionResult.FAIL;
    }

    @Override
    public void onMarkDrawn(MarkDrawingContext drawingContext, Mark mark) {
        Player player = drawingContext.getPlayer();
        if (player.isCreative())
            return;

        InteractionHand drawingHand = drawingContext.getDrawingHand();

        ItemStack result = damageAndDestroy(player.getItemInHand(drawingHand), player);
        if (result.isEmpty())
            player.setItemInHand(drawingHand, ItemStack.EMPTY);
    }

    public static ItemStack damageAndDestroy(ItemStack chalkStack, Player player) {
        if (!chalkStack.isDamageableItem())
            return chalkStack;

        chalkStack.setDamageValue(chalkStack.getDamageValue() + 1);
        if (chalkStack.getDamageValue() >= chalkStack.getMaxDamage()) {
            Vec3 playerPos = player.position();
            player.level.playSound(player, playerPos.x, playerPos.y, playerPos.z, Chalk.SoundEvents.CHALK_BROKEN.get(),
                    SoundSource.PLAYERS, 0.9f, 0.9f + player.level.random.nextFloat() * 0.2f);
            return ItemStack.EMPTY;
        }

        return chalkStack;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return CommonConfig.CHALK_DURABILITY.get();
    }
    public DyeColor getColor() {
        return this.color;
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
