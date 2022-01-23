package io.github.mortuusars.chalk.items;

import com.mojang.math.Vector3f;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.blocks.MarkSymbol;
import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.render.ChalkColors;
import io.github.mortuusars.chalk.setup.ModBlocks;
import io.github.mortuusars.chalk.utils.ClickLocationUtils;
import io.github.mortuusars.chalk.utils.DrawingUtils;
import io.github.mortuusars.chalk.utils.ParticleUtils;
import io.github.mortuusars.chalk.utils.PositionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ChalkItem extends Item {

    private final DyeColor _color;

    public ChalkItem(DyeColor dyeColor, Properties properties) {
        super(properties
                .tab(CreativeModeTab.TAB_DECORATIONS)
                .stacksTo(1)
                .defaultDurability(64)
                .setNoRepair());

        _color = dyeColor;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return CommonConfig.CHALK_DURABILITY.get();
    }

    public DyeColor getColor() {
        return this._color;
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

        final boolean isSecondaryUseActive = context.isSecondaryUseActive();
        final Level level = context.getLevel();
        final BlockPos clickedPos = context.getClickedPos();
        final BlockState clickedBlockState = level.getBlockState(clickedPos);
        final Direction clickedFace = context.getClickedFace();

        final boolean isClickedOnAMark = level.getBlockState(clickedPos).getBlock() instanceof ChalkMarkBlock;

        BlockPos newMarkPosition = isClickedOnAMark ? clickedPos : clickedPos.relative(clickedFace);
        final Direction newMarkFacing = isClickedOnAMark ? level.getBlockState(newMarkPosition).getValue(ChalkMarkBlock.FACING) : clickedFace;
        BlockState newMarkBlockState = getNewMarkBlockState(isSecondaryUseActive, context.getClickLocation(), clickedPos, newMarkFacing);

        if (!isDrawableThere(newMarkPosition, clickedBlockState, clickedPos, newMarkBlockState.getValue(ChalkMarkBlock.FACING), level))
            return InteractionResult.PASS;

        // Cancel drawing if marks are same.
        // Remove old mark if different.
        final BlockState oldMarkBlockState = level.getBlockState(newMarkPosition);
        if (oldMarkBlockState.getBlock() instanceof ChalkMarkBlock) {
            if (oldMarkBlockState == newMarkBlockState)
                return InteractionResult.FAIL;

            // Remove old mark. It would be replaced with new one.
            level.removeBlock(newMarkPosition, false);
        }

        drawMarkAndDamageItem(hand, itemStack, player, level, newMarkFacing, newMarkPosition, newMarkBlockState);
        return InteractionResult.CONSUME;
    }

    private BlockState getNewMarkBlockState(boolean isSecondaryUseActive, Vec3 clickLocation, BlockPos clickedPos, Direction clickedFace) {
        final BlockState defaultBlockState = ModBlocks.getMarkBlockByColor(_color).defaultBlockState().setValue(ChalkMarkBlock.FACING, clickedFace);

        if (isSecondaryUseActive)
            return defaultBlockState.setValue(ChalkMarkBlock.SYMBOL, MarkSymbol.CROSS);
        else {
            final int orientation = ClickLocationUtils.getBlockRegion(clickLocation, clickedPos, clickedFace);
            return defaultBlockState.setValue(ChalkMarkBlock.ORIENTATION, orientation);
        }
    }

    private boolean isDrawableThere(BlockPos markPosition, BlockState drawingTarget, BlockPos targetBlockPos, Direction face, Level level){
        if (drawingTarget.getBlock() instanceof ChalkMarkBlock)
            return true;

        final boolean isFaceFull = Block.isFaceFull(drawingTarget.getCollisionShape(level, targetBlockPos), face);

        final Block blockAtDrawingPos = level.getBlockState(markPosition).getBlock();
        return isFaceFull && (blockAtDrawingPos instanceof AirBlock || blockAtDrawingPos instanceof ChalkMarkBlock);
    }

    private void damageItemStack(InteractionHand hand, ItemStack itemStack, Player player, Level level, BlockPos markPosition) {
        itemStack.setDamageValue(itemStack.getDamageValue() + 1);
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
            level.playSound(null, markPosition, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.75f, 1f);
        }
    }

    private void drawMarkAndDamageItem(InteractionHand hand, ItemStack itemStack, Player player, Level level, Direction facing, BlockPos newMarkPosition, BlockState newMarkBlockState) {
        boolean glowingItemInOffHand = false;
        if (hand == InteractionHand.MAIN_HAND && DrawingUtils.isGlowingItem(player.getOffhandItem().getItem())){
            newMarkBlockState = newMarkBlockState.setValue(ChalkMarkBlock.GLOWING, true);
            glowingItemInOffHand = true;
        }

        if (level.isClientSide)
            ParticleUtils.spawnColorDustParticles(_color, level, newMarkPosition, facing);
        else {
            level.setBlock(newMarkPosition, newMarkBlockState, Block.UPDATE_ALL_IMMEDIATE);

            if (!player.isCreative()){
                damageItemStack(hand, itemStack, player, level, newMarkPosition);
                if (glowingItemInOffHand)
                    player.getOffhandItem().shrink(1);
            }

            level.playSound(null, newMarkPosition, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                    SoundSource.BLOCKS, 0.6f,  new Random().nextFloat() * 0.2f + 0.8f);
        }
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
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
