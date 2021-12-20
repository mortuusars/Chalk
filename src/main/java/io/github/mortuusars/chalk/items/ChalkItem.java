package io.github.mortuusars.chalk.items;

import com.mojang.math.Vector3f;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.blocks.MarkSymbol;
import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.setup.ModBlocks;
import io.github.mortuusars.chalk.utils.ClickLocationUtils;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
        final BlockPos pos = context.getClickedPos();
        final BlockState clickedBlockState = level.getBlockState(pos);
        Direction clickedFace = context.getClickedFace();
        BlockPos markPosition = pos.relative(clickedFace);

        if (clickedBlockState.getBlock() instanceof ChalkMarkBlock) {
            // Replace mark
            clickedFace = clickedBlockState.getValue(ChalkMarkBlock.FACING);
            markPosition = pos;
            level.removeBlock(pos, false);
        }
        else if (!Block.isFaceFull(clickedBlockState.getCollisionShape(level, pos, CollisionContext.of(player)), clickedFace))
            return InteractionResult.PASS;
        else if (!level.isEmptyBlock(markPosition) && !(level.getBlockState(markPosition).getBlock() instanceof ChalkMarkBlock))
            // Surface is suitable but something is blocking the mark
            return InteractionResult.PASS;

        if (level.isClientSide){
            spawnDustParticles(level, clickedFace, markPosition);
            return InteractionResult.CONSUME;
        }

        final int orientation = ClickLocationUtils.getBlockRegion(context.getClickLocation(), pos, clickedFace);

        BlockState blockState = ModBlocks.getMarkBlockByColor(_color).defaultBlockState()
                .setValue(ChalkMarkBlock.FACING, clickedFace)
                .setValue(ChalkMarkBlock.ORIENTATION, orientation);

        if (context.isSecondaryUseActive())
            blockState = blockState.setValue(ChalkMarkBlock.SYMBOL, MarkSymbol.CROSS);

        level.setBlock(markPosition, blockState, Block.UPDATE_ALL_IMMEDIATE);

        if (!player.isCreative()) {
            itemStack.setDamageValue(itemStack.getDamageValue() + 1);
            if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                player.setItemInHand(context.getHand(), ItemStack.EMPTY);
                level.playSound(null, markPosition, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.75f, 1f);
            }
        }

        level.playSound(null, markPosition, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                SoundSource.BLOCKS, 0.6f,  new Random().nextFloat() * 0.2f + 0.8f);

        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isSecondaryUseActive())
            return useOn(context);

        return InteractionResult.PASS;
    }

    private void spawnDustParticles(Level world, Direction clickedFace, BlockPos markPosition) {
        int colorValue = _color.getTextColor();

        float R = (colorValue & 0x00FF0000) >> 16;
        float G = (colorValue & 0x0000FF00) >> 8;
        float B = (colorValue & 0x000000FF);

        ParticleUtils.spawnParticle(world, new DustParticleOptions(new Vector3f(R / 255, G / 255, B / 255), 1.8f),
                PositionUtils.blockCenterOffsetToFace(markPosition, clickedFace, 0.25f), 1);
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
