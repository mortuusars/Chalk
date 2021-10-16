package io.github.mortuusars.chalk.blocks;

import io.github.mortuusars.chalk.setup.ModItems;
import io.github.mortuusars.chalk.utils.ParticleUtils;
import io.github.mortuusars.chalk.utils.PositionUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import java.util.Random;

@SuppressWarnings({"deprecation", "NullableProblems", "PointlessBooleanExpression"})
public class ChalkMarkBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty ORIENTATION = IntegerProperty.create("orientation", 0, 8);
    public static final BooleanProperty GLOWING = BooleanProperty.create("is_glowing");
    public static final EnumProperty<MarkSymbol> SYMBOL = EnumProperty.create("symbol", MarkSymbol.class);

    private final DyeColor _color;

    private static final VoxelShape DOWN_AABB = Block.box(1.5D, 15.5D, 1.5D, 14.5D, 16D, 14.5D);
    private static final VoxelShape UP_AABB = Block.box(1.5D, 0D, 1.5D, 14.5D, 0.5D, 14.5D);
    private static final VoxelShape SOUTH_AABB = Block.box(1.5D, 1.5D, 0D, 14.5D, 14.5D, 0.5D);
    private static final VoxelShape EAST_AABB = Block.box(0D, 1.5D, 1.5D, 0.5D, 14.5D, 14.5D);
    private static final VoxelShape WEST_AABB = Block.box(15.5D, 1.5D, 1.5D, 16D, 14.5D, 14.5D);
    private static final VoxelShape NORTH_AABB = Block.box(1.5D, 1.5D, 15.5D, 14.5D, 14.5D, 16D);

    public ChalkMarkBlock(DyeColor dyeColor, Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ORIENTATION, 4)
                .setValue(GLOWING, false)
                .setValue(SYMBOL, MarkSymbol.NONE));

        _color = dyeColor;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        if (player.isCreative())
            return new ItemStack(ModItems.getChalkByColor(_color));

        ItemStack item = getMatchingItemStack(player, ModItems.getChalkByColor(_color));
        return item == ItemStack.EMPTY ? new ItemStack(ModItems.getChalkByColor(_color)) : item;
    }

    private ItemStack getMatchingItemStack(PlayerEntity player, Item item){
        return player.inventory.items.stream().filter(invItem ->
                invItem.getItem().getRegistryName() == item.getRegistryName()).findFirst().orElse(ItemStack.EMPTY);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(ORIENTATION).add(GLOWING).add(SYMBOL);
    }

    public DyeColor getColor() {
        return _color;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext selectionContext) {
        Direction facing = state.getValue(FACING);

        switch (facing) {
            case UP:
                return UP_AABB;
            case NORTH:
                return NORTH_AABB;
            case WEST:
                return WEST_AABB;
            case EAST:
                return EAST_AABB;
            case SOUTH:
                return SOUTH_AABB;
            default:
                return DOWN_AABB;
        }
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {

        if (blockState.getValue(GLOWING))
            return ActionResultType.PASS;

        ItemStack usedItem = player.getItemInHand(hand);

        if (isGlowingItem(usedItem.getItem())) {

            if (world.setBlock(blockPos, blockState.setValue(GLOWING, true), Constants.BlockFlags.DEFAULT_AND_RERENDER)) {

                if (!player.isCreative()) {
                    int itemsCount = usedItem.getCount();
                    if (itemsCount-- <= 0)
                        player.setItemInHand(hand, ItemStack.EMPTY);
                    else
                        usedItem.setCount(itemsCount);
                }

                world.playSound(null, blockPos, SoundEvents.TURTLE_SHAMBLE, SoundCategory.BLOCKS, 1.5f, 1f);
                ParticleUtils.spawnParticle(world, ParticleTypes.END_ROD, PositionUtils.blockFaceCenter(blockPos, blockState.getValue(FACING),
                        0.3f), new Vector3f(0f, 0.03f, 0f), 2);

                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }

    private boolean isGlowingItem(Item item){
        if (item == Items.GLOWSTONE_DUST)
            return true;

        ResourceLocation itemRegistryName = item.getRegistryName();

        return itemRegistryName != null && (
               itemRegistryName.getPath().contains("glow_ink_sac") ||
               itemRegistryName.getPath().contains("glowing_ink_sac"));
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        return removeMark(world, pos, false);
    }

    private boolean removeMark(World world, BlockPos pos, boolean isMoving) {

        Direction facing = world.getBlockState(pos).getValue(FACING);

        if (world.removeBlock(pos, isMoving)) {
            if (!world.isClientSide())
                world.playSound(null, pos, SoundEvents.WART_BLOCK_HIT, SoundCategory.BLOCKS, 0.5f, new Random().nextFloat() * 0.2f + 0.8f);
            else {
                int colorValue = _color.getColorValue();

                float R = (colorValue & 0x00FF0000) >> 16;
                float G = (colorValue & 0x0000FF00) >> 8;
                float B = (colorValue & 0x000000FF);

                ParticleUtils.spawnParticle(world, new RedstoneParticleData(R / 255, G / 255, B / 255, 2f),
                        PositionUtils.blockFaceCenter(pos, facing, 0.25f), 1);
            }
            return true;
        }

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState blockState, World world, BlockPos blockPos, Random random) {
        if (blockState.getValue(GLOWING) == false)
            return;

        if (random.nextInt(30) == 0) {
            ParticleUtils.spawnParticle(world, ParticleTypes.END_ROD, PositionUtils.blockFaceCenter(blockPos, blockState.getValue(FACING),
                    0.33f), new Vector3f(0f, 0.015f, 0f), 1);
        }
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        if (state.getValue(GLOWING))
            return 5;
        else
            return 0;
    }

    @Override
    public void attack(BlockState blockState, World world, BlockPos pos, PlayerEntity player) {
        removeMark(world, pos, false);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        BlockPos relative = pos.relative(state.getValue(FACING).getOpposite());

        if (relative.equals(fromPos)) {
            removeMark(world, pos, isMoving);
        }
    }

    @Override
    public boolean isPathfindable(BlockState p_196266_1_, IBlockReader p_196266_2_, BlockPos p_196266_3_, PathType p_196266_4_) {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.DESTROY;
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockItemUseContext blockItemUseContext) {
        return true;
    }
}
