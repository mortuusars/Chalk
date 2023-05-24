package io.github.mortuusars.chalk.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.math.Vector3f;
import io.github.mortuusars.chalk.Chalk;
import io.github.mortuusars.chalk.config.Config;
import io.github.mortuusars.chalk.core.MarkSymbol;
import io.github.mortuusars.chalk.core.SymbolOrientation;
import io.github.mortuusars.chalk.render.ChalkColors;
import io.github.mortuusars.chalk.utils.ParticleUtils;
import io.github.mortuusars.chalk.utils.PositionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"deprecation", "NullableProblems"})
public class ChalkMarkBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<MarkSymbol> SYMBOL = EnumProperty.create("symbol", MarkSymbol.class);
    public static final EnumProperty<SymbolOrientation> ORIENTATION = EnumProperty.create("orientation", SymbolOrientation.class);
    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");

    private final DyeColor color;

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
                .setValue(SYMBOL, MarkSymbol.CENTER)
                .setValue(ORIENTATION, SymbolOrientation.NORTH)
                .setValue(GLOWING, false));
        color = dyeColor;
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
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState blockState) {
        return new ItemStack(Chalk.Items.getChalk(this.color));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if (player.isCreative())
            return new ItemStack(Chalk.Items.getChalk(color));

        ItemStack item = getMatchingItemStack(player, Chalk.Items.getChalk(color));
        return item == ItemStack.EMPTY ? new ItemStack(Chalk.Items.getChalk(color)) : item;
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
        return color;
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
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        ItemStack usedStack = player.getItemInHand(hand);

        if (!blockState.getValue(GLOWING) && usedStack.is(Chalk.Tags.Items.GLOWINGS)) {
            if (level.setBlock(blockPos, blockState.setValue(GLOWING, true), Block.UPDATE_ALL_IMMEDIATE)) {
                if (!player.isCreative())
                    usedStack.shrink(1);

                level.playSound(null, blockPos, Chalk.SoundEvents.GLOW_APPLIED.get(), SoundSource.BLOCKS, 1f, 1f);
                level.playSound(null, blockPos, Chalk.SoundEvents.GLOWING.get(), SoundSource.BLOCKS, 0.8f, 1f);
                ParticleUtils.spawnParticle(level, ParticleTypes.END_ROD, PositionUtils.blockCenterOffsetToFace(blockPos, blockState.getValue(FACING),
                        0.3f), new Vector3f(0f, 0.03f, 0f), 2);

                return InteractionResult.SUCCESS;
            }
            else
                return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return removeMarkWithEffects(level, pos);
    }

    private boolean removeMarkWithEffects(Level level, BlockPos pos) {
        Direction facing = level.getBlockState(pos).getValue(FACING); // Get facing before removing the block.

        if (level.removeBlock(pos, false)) {
            level.playSound(null, pos, Chalk.SoundEvents.MARK_REMOVED.get(), SoundSource.BLOCKS, 0.5f, new Random().nextFloat() * 0.2f + 0.8f);

            if (level instanceof ServerLevel serverLevel) {
                int colorValue = ChalkColors.fromDyeColor(color);
                float R = (colorValue & 0x00FF0000) >> 16;
                float G = (colorValue & 0x0000FF00) >> 8;
                float B = (colorValue & 0x000000FF);

                Vector3f centerOffset = PositionUtils.blockCenterOffsetToFace(pos, facing, 0.25f);
                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(R / 255, G / 255, B / 255), 2f),
                        centerOffset.x(), centerOffset.y(), centerOffset.z(),
                        1, 0.1, 0.1, 0.1, 0.02);
            }
            else {
                ParticleUtils.spawnColorDustParticles(color, level, pos, facing);
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
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(GLOWING)) {
            if (randomSource.nextInt(90) == 0) {
                ParticleUtils.spawnParticle(level, ParticleTypes.END_ROD, PositionUtils.blockCenterOffsetToFace(blockPos, blockState.getValue(FACING),
                        0.33f), new Vector3f(0f, 0.015f, 0f), 1);
            }
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(GLOWING) ? Config.GLOWING_CHALK_MARK_LIGHT_LEVEL.get() : 0;
    }

    @Override
    public void attack(BlockState blockState, Level level, BlockPos pos, Player player) {
        removeMarkWithEffects(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!state.canSurvive(level, pos)) {
            removeMarkWithEffects(level, pos);
            return;
        }

        BlockPos surfacePos = pos.relative(state.getValue(FACING).getOpposite());
        if (surfacePos.equals(fromPos) && level.getBlockState(surfacePos).getBlock() instanceof GrassBlock) {
            level.removeBlock(pos, false);
            level.playSound(null, pos, Chalk.SoundEvents.MARK_REMOVED.get(), SoundSource.BLOCKS, 0.5f, new Random().nextFloat() * 0.2f + 0.8f);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos surfacePos = pos.relative(facing.getOpposite());
        BlockState surfaceBlockState = level.getBlockState(surfacePos);
        return surfaceBlockState.isFaceSturdy(level, surfacePos, facing);
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

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction facing = state.getValue(FACING);
        BlockState rotated = state.setValue(FACING, rotation.rotate(facing));

        boolean yAxis = facing.getAxis() == Direction.Axis.Y;
        boolean canRotateSymbol = state.getValue(SYMBOL).getOrientationBehavior() != MarkSymbol.OrientationBehavior.FIXED;
        return yAxis && canRotateSymbol ? rotated.setValue(ORIENTATION, state.getValue(ORIENTATION)
                .rotate(rotation))
                : rotated;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}