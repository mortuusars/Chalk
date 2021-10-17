package io.github.mortuusars.chalk.items;

import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.blocks.MarkSymbol;
import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.setup.ModBlocks;
import io.github.mortuusars.chalk.utils.ClickLocationUtils;
import io.github.mortuusars.chalk.utils.ParticleUtils;
import io.github.mortuusars.chalk.utils.PositionUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class ChalkItem extends Item {

    private final DyeColor _color;

    public ChalkItem(DyeColor dyeColor, Properties properties) {
        super(properties
                .tab(ItemGroup.TAB_DECORATIONS)
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

    //This is called when the item is used, before the block is activated.
    //Return PASS to allow vanilla handling, any other to skip normal code.
    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (stack.getItem() != this)
            return ActionResultType.FAIL;

        final World world = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        final BlockState clickedBlockState = world.getBlockState(pos);
        BlockPos markPosition = pos.relative(clickedFace);
        final PlayerEntity player = context.getPlayer();

        // Do not draw from offhand if drawn from main hand
        if (context.getHand() == Hand.OFF_HAND && player.getMainHandItem().getItem() instanceof ChalkItem)
            return ActionResultType.FAIL;

        if (clickedBlockState.getBlock() instanceof ChalkMarkBlock) {
            // Replace mark
            clickedFace = clickedBlockState.getValue(ChalkMarkBlock.FACING);
            markPosition = pos;
            world.removeBlock(pos, false);
        }
        else if (!Block.isFaceFull(clickedBlockState.getCollisionShape(world, pos, ISelectionContext.of(player)), clickedFace))
            return ActionResultType.PASS;
        else if (!world.isEmptyBlock(markPosition) && !(world.getBlockState(markPosition).getBlock() instanceof ChalkMarkBlock))
            // Surface is suitable but something is blocking the mark
            return ActionResultType.PASS;

        if (world.isClientSide){
            spawnDustParticles(world, clickedFace, markPosition);
            return ActionResultType.CONSUME;
        }

        final int orientation = ClickLocationUtils.getBlockRegion(context.getClickLocation(), pos, clickedFace);

        BlockState blockState = ModBlocks.getMarkBlockByColor(_color).defaultBlockState()
                .setValue(ChalkMarkBlock.FACING, clickedFace)
                .setValue(ChalkMarkBlock.ORIENTATION, orientation);

        if (context.isSecondaryUseActive())
            blockState = blockState.setValue(ChalkMarkBlock.SYMBOL, MarkSymbol.CROSS);

        world.setBlock(markPosition, blockState, Constants.BlockFlags.DEFAULT_AND_RERENDER);

        if (!player.isCreative()) {
            stack.setDamageValue(stack.getDamageValue() + 1);
            if (stack.getDamageValue() >= stack.getMaxDamage()) {
                player.setItemInHand(context.getHand(), ItemStack.EMPTY);
                world.playSound(null, markPosition, SoundEvents.GRAVEL_BREAK, SoundCategory.BLOCKS, 0.65f, 1f);
            }
        }

        world.playSound(null, markPosition, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 0.6f, random.nextFloat() * 0.2f + 0.8f);

        return ActionResultType.CONSUME;
    }

    private void spawnDustParticles(World world, Direction clickedFace, BlockPos markPosition) {
        int colorValue = _color.getColorValue();

        float R = (colorValue & 0x00FF0000) >> 16;
        float G = (colorValue & 0x0000FF00) >> 8;
        float B = (colorValue & 0x000000FF);

        ParticleUtils.spawnParticle(world, new RedstoneParticleData(R / 255, G / 255, B / 255, 1.8f),
                PositionUtils.blockFaceCenter(markPosition, clickedFace, 0.25f), 1);
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
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
