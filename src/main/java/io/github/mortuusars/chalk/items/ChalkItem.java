package io.github.mortuusars.chalk.items;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.core.Mark;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.client.gui.SymbolSelectScreen;
import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.core.ChalkMark;
import io.github.mortuusars.chalk.core.SymbolOrientation;
import io.github.mortuusars.chalk.utils.MarkDrawingContext;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
        final Level level = context.getLevel();

        if (player == null || !(itemStack.getItem() instanceof ChalkItem))
            return InteractionResult.FAIL;

        // When holding chalks in both hands - skip drawing from offhand
        if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof ChalkItem)
            return InteractionResult.FAIL;

        BlockPos pos = context.getClickedPos();
        Direction facing = context.getClickedFace();
        boolean isGlowing = player.getOffhandItem().is(Chalk.Tags.Items.GLOWINGS);

        if (level.getBlockState(pos).getBlock() instanceof ChalkMarkBlock) {
            facing = level.getBlockState(pos).getValue(ChalkMarkBlock.FACING);
            pos = pos.relative(facing.getOpposite());
        }

        MarkDrawingContext drawingContext = new MarkDrawingContext(level, player,
                new BlockHitResult(context.getClickLocation(), facing, pos, false));

        if (!drawingContext.canDraw())
            return InteractionResult.FAIL;

        if (!player.isSecondaryUseActive()) {
            MarkSymbol symbol = drawingContext.getInitialOrientation() == SymbolOrientation.CENTER ? MarkSymbol.CENTER : MarkSymbol.ARROW;
            Mark mark = drawingContext.createMark(color, symbol, isGlowing);

            if (drawingContext.hasExistingMark() && !drawingContext.shouldMarkReplaceAnother(mark))
                return InteractionResult.FAIL;

            if (drawingContext.draw(mark)) {
                if (!player.isCreative())
                    damageAndConsumeItems(hand, itemStack, player, level, isGlowing);

                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            }

            return InteractionResult.FAIL;
        }
        else {
            MarkSymbol symbol = MarkSymbol.CROSS;
            Mark mark = drawingContext.createMark(color, symbol, isGlowing);

            if (drawingContext.hasExistingMark() && !drawingContext.shouldMarkReplaceAnother(mark))
                return InteractionResult.FAIL;

            if (drawingContext.draw(mark)) {
                if (!player.isCreative())
                    damageAndConsumeItems(hand, itemStack, player, level, isGlowing);

                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            }

                //            if (level.isClientSide) {
                //                SymbolSelectScreen symbolSelectScreen = new SymbolSelectScreen(context);
                //                Minecraft.getInstance().setScreen(symbolSelectScreen);
                //            }
                //            return InteractionResult.CONSUME;
                //        }

            return InteractionResult.FAIL;
        }
    }

    private void damageAndConsumeItems(InteractionHand hand, ItemStack itemStack, Player player, Level level, boolean isGlowing) {
        if (!itemStack.isDamageableItem())
            return;

        itemStack.setDamageValue(itemStack.getDamageValue() + 1);
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
            Vec3 playerPos = player.position();
            level.playSound(player, playerPos.x, playerPos.y, playerPos.z, Chalk.SoundEvents.CHALK_BROKEN.get(),
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
