package io.github.mortuusars.chalk.world.item;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.Config;
import io.github.mortuusars.chalk.world.chalk.Mark;
import io.github.mortuusars.chalk.world.chalk.MarkDrawingContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

public class ChalkItem extends Item implements MarkDrawable {
    public ChalkItem(Properties properties) {
        super(properties);
    }

    public int getColorFromDye(ItemStack stack, DyeColor dye) {
        return getColorFromDye(dye);
    }

    @Override
    public int getMarkDrawingColor(ItemStack stack) {
        return DyedItemColor.getOrDefault(stack, 0xFFFFFF);
    }

    public int getTintColor(ItemStack stack, int index) {
        return ARGB.opaque(getMarkDrawingColor(stack));
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack itemStack) {
        //TODO: item_name should probably be used instead of this
        if (Config.Server.DYED_CHALK_NAMES.get()) {
            if (!(itemStack.get(DataComponents.DYED_COLOR) instanceof DyedItemColor dyedColor)) {
                return Component.translatable("item.chalk.white_chalk");
            }

            if (Config.Server.CHALK_COLORS.inverse().get(dyedColor.rgb()) instanceof DyeColor dyeColor) {
                return Component.translatable("item.chalk." + dyeColor.getName() + "_chalk");
            }
        }

        return super.getName(itemStack);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(@NonNull ItemStack itemStack, @NonNull TooltipContext context, @NonNull TooltipDisplay display,
                                @NonNull Consumer<Component> builder, @NonNull TooltipFlag tooltipFlag) {
        if (!itemStack.has(DataComponents.DYED_COLOR)) {
            builder.accept(Component.translatable("item.chalk.chalk.tooltip.dyeable").withStyle(ChatFormatting.GRAY));
        }
    }

    // --

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if (player == null) {
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
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    // -- Drawable

    @Override
    public void onMarkDrawn(Player player, MarkDrawingContext context, Mark mark) {
        MarkDrawable.super.onMarkDrawn(player, context, mark);
        ItemStack stack = player.getItemInHand(context.hand());
        onChalkMarkDrawn(player, context.hand(), stack, context.markPos(), context.markFacing(), mark);
        if (!player.isCreative()) {
            stack.hurtAndBreak(1, player, context.hand().asEquipmentSlot());
        }
    }

    public void onChalkMarkDrawn(Player player, InteractionHand hand, ItemStack stack, BlockPos markPos, Direction markFacing, Mark mark) {
        if (player instanceof ServerPlayer serverPlayer) {
            Chalk.CriteriaTriggers.MARK_DRAWN.get().trigger(serverPlayer, stack, markPos, mark, markPos.relative(markFacing.getOpposite()));
        }
    }

    // --

    public static int getColorFromDye(DyeColor dye) {
        return Config.Server.CHALK_COLORS.getOrDefault(dye, dye.getTextureDiffuseColor());
    }

    public static ItemStack create(int color, int damage) {
        ItemStack stack = new ItemStack(Chalk.Items.CHALK.get());
        if (color != 0xFFFFFF) stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        if (damage > 0) stack.setDamageValue(damage);
        return stack;
    }
}
