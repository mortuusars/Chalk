package io.github.mortuusars.chalk.Blocks;

import io.github.mortuusars.chalk.setup.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.Item;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

@SuppressWarnings({"deprecation", "NullableProblems"})
public class ChalkMarkBlock extends Block {

//    final VoxelShape[] voxelShape;

    public ChalkMarkBlock(Properties properties) {
        super(properties);

//        VoxelShape shape = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    }

    @Override
    public Item asItem() {
        return ModItems.CHALK.get();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_220071_1_, IBlockReader p_220071_2_, BlockPos p_220071_3_, ISelectionContext p_220071_4_) {
        return VoxelShapes.empty();
    }

    @Override
    public boolean isPathfindable(BlockState p_196266_1_, IBlockReader p_196266_2_, BlockPos p_196266_3_, PathType p_196266_4_) {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.DESTROY;
    }
}
