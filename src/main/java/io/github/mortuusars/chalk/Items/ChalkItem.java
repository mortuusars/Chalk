package io.github.mortuusars.chalk.Items;

import io.github.mortuusars.chalk.Blocks.ChalkMarkBlock;
import io.github.mortuusars.chalk.setup.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class ChalkItem extends Item {

    public ChalkItem(Properties properties) {
        super(properties
                .tab(ItemGroup.TAB_TOOLS)
                .stacksTo(1)
                .defaultDurability(64)
                .setNoRepair());
    }

    //This is called when the item is used, before the block is activated.
    //Return PASS to allow vanilla handling, any other to skip normal code.
    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {

        final World world = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        final BlockState clickedBlockState = world.getBlockState(pos);
        BlockPos markPosition = pos.relative(clickedFace);
        final PlayerEntity player = context.getPlayer();

        if (clickedBlockState.getBlock() == ModBlocks.CHALK_MARK_BLOCK.get()){ // replace mark
            clickedFace = clickedBlockState.getValue(ChalkMarkBlock.FACING);
            markPosition = pos;
            world.removeBlock(pos, false);
        }
        else if (!Block.isFaceFull(clickedBlockState.getCollisionShape(world, pos, ISelectionContext.of(player)), clickedFace))
            return ActionResultType.PASS;
        else if ((!world.isEmptyBlock(markPosition) && world.getBlockState(markPosition).getBlock() != ModBlocks.CHALK_MARK_BLOCK.get()) ||
                stack.getItem() != this)
            return ActionResultType.PASS;

        if (world.isClientSide()) {
            Random r = new Random();
            world.addParticle(ParticleTypes.CLOUD, markPosition.getX() + (0.5 * (r.nextFloat() + 0.4)), markPosition.getY() + 0.65, markPosition.getZ() + (0.5 * (r.nextFloat() + 0.4)), 0.0D, 0.05D, 0.0D);
            return ActionResultType.PASS;
        }

        final int orientation = getClickedRegion(context.getClickLocation(), clickedFace);

        BlockState blockState = ModBlocks.CHALK_MARK_BLOCK.get().defaultBlockState()
                .setValue(ChalkMarkBlock.FACING, clickedFace)
                .setValue(ChalkMarkBlock.ORIENTATION, orientation);

        if (world.setBlock(markPosition, blockState, 1 | 2)) {

            if (!player.isCreative()) {
                stack.setDamageValue(stack.getDamageValue() + 1);
                if (stack.getDamageValue() >= stack.getMaxDamage()) {
                    player.setItemInHand(context.getHand(), ItemStack.EMPTY);
                    world.playSound(null, markPosition, SoundEvents.GRAVEL_BREAK, SoundCategory.BLOCKS, 0.5f, 1f);
                }
            }

            world.playSound(null, markPosition, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 0.8f, random.nextFloat() * 0.2f + 0.8f);
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.FAIL;
    }

    private int getClickedRegion(Vector3d clickLocation, Direction face) {

        // Calculates which region of the block was clicked:
        // Matrix represents the block regions:
        final int[][] blockRegions = new int[][] {
                new int[] {0,1,2},
                new int[] {3,4,5},
                new int[] {6,7,8}
        };

        final double x = clickLocation.x;
        final double y = clickLocation.y;
        final double z = clickLocation.z;

        // Remove whole number: 21.31 => 0.31
        final double fracx = x - (int)x;
        final double fracz = z - (int)z;

        // Normalize negative values
        final double dx = fracx > 0 ? fracx : fracx + 1;
        final double dy = y - (int)y;
        final double dz = fracz > 0 ? fracz : fracz + 1;

        if (face == Direction.UP || face == Direction.DOWN){
            final int xpart = Math.min(2, (int)(dx / 0.333));
            final int zpart = Math.min(2, (int)(dz / 0.333));

            return blockRegions[zpart][xpart];
        }
        else if (face == Direction.NORTH || face == Direction.SOUTH) {
            final int xpart = Math.min(2, (int)(dx / 0.333));
            final int ypart = Math.min(2, (int)((1 - dy) / 0.333));

            return blockRegions[ypart][xpart];
        }
        else if (face == Direction.WEST || face == Direction.EAST) {
            final int zpart = Math.min(2, (int)(dz / 0.333));
            final int ypart = Math.min(2, (int)((1 - dy) / 0.333));

            return blockRegions[ypart][zpart];
        }
        else
            return 4; // Center of the block by default
    }
}
