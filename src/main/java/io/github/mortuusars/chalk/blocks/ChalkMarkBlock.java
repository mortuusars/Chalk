package io.github.mortuusars.chalk.blocks;

import com.google.common.collect.ImmutableMap;
import com.mojang.math.Vector3f;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.CommonConfig;
import io.github.mortuusars.chalk.setup.ModItems;
import io.github.mortuusars.chalk.setup.ModTags;
import io.github.mortuusars.chalk.utils.DrawingUtils;
import io.github.mortuusars.chalk.utils.ParticleUtils;
import io.github.mortuusars.chalk.utils.PositionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfig;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"deprecation", "NullableProblems"})
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

    private final Map<BlockState, VoxelShape> shapesCache;

    public ChalkMarkBlock(DyeColor dyeColor, Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ORIENTATION, 4)
                .setValue(GLOWING, false)
                .setValue(SYMBOL, MarkSymbol.NONE));

        _color = dyeColor;

        shapesCache = ImmutableMap.copyOf(this.getStateDefinition().getPossibleStates().stream().collect(Collectors.toMap(Function.identity(), ChalkMarkBlock::calculateShapes)));
    }

    private static VoxelShape calculateShapes(BlockState blockState) {
        return switch (blockState.getValue(FACING)) {
            case DOWN -> DOWN_AABB;
            case UP -> UP_AABB;
            case NORTH -> NORTH_AABB;
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            case EAST -> EAST_AABB;
        };
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return shapesCache.get(blockState);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState blockState) {
        return new ItemStack(ModItems.getChalkByColor(this._color));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        if (player.isCreative())
            return new ItemStack(ModItems.getChalkByColor(_color));

        ItemStack item = getMatchingItemStack(player, ModItems.getChalkByColor(_color));
        return item == ItemStack.EMPTY ? new ItemStack(ModItems.getChalkByColor(_color)) : item;
    }

    private ItemStack getMatchingItemStack(Player player, Item item){
        return player.getInventory().items.stream().filter(invItem ->
                invItem.getItem().builtInRegistryHolder().key().location() == item.builtInRegistryHolder().key().location()).findFirst().orElse(ItemStack.EMPTY);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(ORIENTATION).add(GLOWING).add(SYMBOL);
    }

    public DyeColor getColor() {
        return _color;
    }

    @Override
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        Direction facing = blockState.getValue(FACING);

        return switch (facing) {
            case UP -> UP_AABB;
            case NORTH -> NORTH_AABB;
            case WEST -> WEST_AABB;
            case EAST -> EAST_AABB;
            case SOUTH -> SOUTH_AABB;
            default -> DOWN_AABB;
        };
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState state, boolean p_60570_) {
        super.onPlace(blockState, level, pos, state, p_60570_);

        if (level.isClientSide){
            Chalk.LOGGER.info(blockState);
            Chalk.LOGGER.info(state);
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {

        if (blockState.getValue(GLOWING))
            return InteractionResult.PASS;

        ItemStack usedItem = player.getItemInHand(hand);

        if (usedItem.is(ModTags.Items.GLOWING)) {

            if (world.setBlock(blockPos, blockState.setValue(GLOWING, true), Block.UPDATE_ALL_IMMEDIATE)) {
                if (!player.isCreative()) {
                    int itemsCount = usedItem.getCount();
                    if (itemsCount-- <= 0)
                        player.setItemInHand(hand, ItemStack.EMPTY);
                    else
                        usedItem.setCount(itemsCount);
                }

                world.playSound(null, blockPos, SoundEvents.TURTLE_SHAMBLE, SoundSource.BLOCKS, 1.5f, 1f);
                ParticleUtils.spawnParticle(world, ParticleTypes.END_ROD, PositionUtils.blockCenterOffsetToFace(blockPos, blockState.getValue(FACING),
                        0.3f), new Vector3f(0f, 0.03f, 0f), 2);

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return removeMark(world, pos, false);
    }

    private boolean removeMark(Level world, BlockPos pos, boolean isMoving) {
        Direction facing = world.getBlockState(pos).getValue(FACING); // Get facing before removing the block.

        if (world.removeBlock(pos, isMoving)) {
            if (!world.isClientSide())
                world.playSound(null, pos, SoundEvents.WART_BLOCK_HIT, SoundSource.BLOCKS, 0.5f, new Random().nextFloat() * 0.2f + 0.8f);
            else {
                ParticleUtils.spawnColorDustParticles(_color, world, pos, facing);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState blockState, Level world, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(GLOWING)) {
            if (randomSource.nextInt(90) == 0) {
                ParticleUtils.spawnParticle(world, ParticleTypes.END_ROD, PositionUtils.blockCenterOffsetToFace(blockPos, blockState.getValue(FACING),
                        0.33f), new Vector3f(0f, 0.015f, 0f), 1);
            }
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getValue(GLOWING) ? CommonConfig.GLOWING_CHALK_MARK_LIGHT_LEVEL.get() : 0;
    }

    @Override
    public void attack(BlockState blockState, Level world, BlockPos pos, Player player) {
        removeMark(world, pos, false);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        BlockPos relative = pos.relative(state.getValue(FACING).getOpposite());

        if (relative.equals(fromPos)) {
            removeMark(world, pos, isMoving);
        }
    }

    @Override
    public boolean isPathfindable(BlockState p_196266_1_, BlockGetter blockGetter, BlockPos p_196266_3_, PathComputationType p_196266_4_) {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.DESTROY;
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockItemUseContext) {
        return true;
    }
}