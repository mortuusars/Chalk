package io.github.mortuusars.chalk.Items;

import io.github.mortuusars.chalk.setup.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChalkItem extends Item {

    public ChalkItem(Properties properties) {
        super(properties
                .tab(ItemGroup.TAB_TOOLS)
                .stacksTo(1)
                .defaultDurability(10)
                .setNoRepair());
    }

    //This is called when the item is used, before the block is activated.
    //Return PASS to allow vanilla handling, any other to skip normal code.
    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if(context.getLevel().isClientSide())
            return ActionResultType.PASS;

        final World world = context.getLevel();
        final PlayerEntity player = context.getPlayer();
        final BlockPos markPosition = context.getClickedPos().above();

        BlockState blockState = ModBlocks.CHALK_MARK_BLOCK.get().defaultBlockState();

        if (world.setBlock(markPosition, blockState, 1|2)){

            stack.setDamageValue(stack.getDamageValue() + 1);
            if (stack.getDamageValue() >= stack.getMaxDamage()){
                player.setItemInHand(context.getHand(), ItemStack.EMPTY);
                world.playSound(null, markPosition, SoundEvents.GRAVEL_BREAK, SoundCategory.BLOCKS,0.6f, 1f);
            }

            world.playSound(null, markPosition, SoundEvents.CHORUS_FLOWER_DEATH, SoundCategory.BLOCKS, 0.9f, random.nextFloat() * 0.2f + 0.9f);
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.FAIL;
    }
}
