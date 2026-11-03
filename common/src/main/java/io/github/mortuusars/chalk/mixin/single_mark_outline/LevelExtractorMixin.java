package io.github.mortuusars.chalk.mixin.single_mark_outline;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.mortuusars.chalk.world.block.MarkBlock;
import io.github.mortuusars.chalk.world.chalk.DrawnMark;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(method = "extractBlockOutline",
          at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private VoxelShape onRenderHitOutline(BlockState instance, BlockGetter blockGetter, BlockPos pos,
                                          CollisionContext collisionContext, Operation<VoxelShape> original) {
        if (instance.getBlock() instanceof MarkBlock
              && minecraft.hitResult instanceof BlockHitResult hitResult
              && MarkBlock.getMarkAt(blockGetter, hitResult.getLocation()) instanceof DrawnMark mark) {
            return MarkBlock.SHAPES[mark.facing().get3DDataValue()];
        }

        return original.call(instance, blockGetter, pos, collisionContext);
    }
}
