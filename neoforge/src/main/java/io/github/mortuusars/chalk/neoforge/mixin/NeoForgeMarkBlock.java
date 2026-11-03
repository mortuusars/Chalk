package io.github.mortuusars.chalk.neoforge.mixin;

import io.github.mortuusars.chalk.world.block.MarkBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MarkBlock.class, remap = false)
public abstract class NeoForgeMarkBlock extends Block implements IBlockExtension {
    public NeoForgeMarkBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state,
                                                boolean includeData, @NotNull Player player) {
        HitResult target = player.pick(player.blockInteractionRange(), 1.0F, false);
        if (target instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(pos)) {
            return ((MarkBlock)(Object)this).pick(player, level, pos, state, blockHit);
        }
        return super.getCloneItemStack(level, pos, state, includeData, player);
    }
}
