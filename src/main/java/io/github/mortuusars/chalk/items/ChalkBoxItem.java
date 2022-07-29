package io.github.mortuusars.chalk.items;

import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.blocks.MarkSymbol;
import io.github.mortuusars.chalk.core.ChalkMark;
import io.github.mortuusars.chalk.menus.ChalkBoxItemStackHandler;
import io.github.mortuusars.chalk.menus.ChalkBoxMenu;
import io.github.mortuusars.chalk.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class ChalkBoxItem extends Item {

    public ChalkBoxItem(Properties properties) {
        super(properties);
    }


    @SuppressWarnings("ConstantConditions")
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
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {


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

        final boolean isClickedOnAMark = level.getBlockState(clickedPos).is(ModTags.Blocks.CHALK_MARK);
        final boolean isGlowing = ChalkBox.getGlowingUses(chalkBoxStack) > 0;

        BlockPos newMarkPosition = isClickedOnAMark ? clickedPos : clickedPos.relative(clickedFace);
        final Direction newMarkFacing = isClickedOnAMark ? level.getBlockState(newMarkPosition).getValue(ChalkMarkBlock.FACING) : clickedFace;

        BlockState markBlockState = ChalkMark.createMarkBlockState(symbol, chalkColor, newMarkFacing, context.getClickLocation(), clickedPos, isGlowing);

        if (isClickedOnAMark) {
            BlockState oldMarkBlockState = level.getBlockState(newMarkPosition);
            if (markBlockState == oldMarkBlockState)
                return InteractionResult.FAIL;

            // Remove old mark. It would be replaced with new one.
            level.removeBlock(newMarkPosition, false);
        }

        if (ChalkMark.drawMark(markBlockState, newMarkPosition, level)) {
            ItemStack chalkItemStack = chalkStack.getFirst();

            chalkItemStack.setDamageValue(chalkItemStack.getDamageValue() + 1);
            if (chalkItemStack.getDamageValue() >= chalkItemStack.getMaxDamage()){
                chalkItemStack = ItemStack.EMPTY;
                Vec3 playerPos = player.position();
                level.playSound(player, playerPos.x, playerPos.y, playerPos.z, SoundEvents.GRAVEL_BREAK,
                        SoundSource.BLOCKS, 0.9f, 0.9f + level.random.nextFloat() * 0.2f);
            }

            ChalkBox.setSlot(chalkBoxStack, chalkStack.getSecond(), chalkItemStack);

            if (isGlowing) {
                ChalkBox.useGlow(chalkBoxStack);
            }
        }

        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {

        ItemStack usedStack = player.getItemInHand(usedHand);

        if (!usedStack.is(this) || !(player instanceof ServerPlayer))
            return InteractionResultHolder.pass(usedStack);

        if (!level.isClientSide){
            NetworkHooks.openGui((ServerPlayer) player,
                    new SimpleMenuProvider( (containerID, playerInventory, playerEntity) ->
                            new ChalkBoxMenu(containerID, playerInventory, usedStack, new ChalkBoxItemStackHandler(usedStack)),
                            new TranslatableComponent("chalk.container.chalk_box")), buffer -> buffer.writeItem(usedStack.copy()));
        }

        return InteractionResultHolder.sidedSuccess(usedStack, level.isClientSide);
    }

    @SuppressWarnings("ConstantConditions")
    private @Nullable DyeColor getSelectedColor(ItemStack stack){
        return stack.hasTag() ? decodeColor(stack.getTag().getFloat(ChalkBox.SELECTED_CHALK_TAG_KEY)) : null;
    }

    private @Nullable DyeColor decodeColor(float value){
        if (value < 1)
            return null;

        return DyeColor.byId(((int) (value - 1)));
    }

    private float encodeColor(DyeColor color){
        return color.getId() + 1;
    }



    private Pair<ItemStack, Integer> getFirstChalkStack(ItemStack chalkBoxStack) {
        for (int slot = 0; slot < ChalkBox.CHALK_SLOTS; slot++) {
            ItemStack itemInSlot = ChalkBox.getItemInSlot(chalkBoxStack, slot);
            if (itemInSlot.is(ModTags.Items.CHALK)) {
                return Pair.of(itemInSlot, slot);
            }
        }

        return null;
    }


}
