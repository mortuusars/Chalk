package io.github.mortuusars.chalk.mixin.chalk_item_particles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.mortuusars.chalk.world.item.ChalkItem;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @SuppressWarnings("LocalMayUseName")
    @WrapOperation(method = "spawnItemParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void onSpawnItemParticle(Level instance, ParticleOptions particleData, double x, double y, double z,
                                     double xd, double yd, double zd, Operation<Void> original,
                                     @Local(argsOnly = true) ItemStack itemStack) {
        if (itemStack.getItem() instanceof ChalkItem chalkItem) {
            int color = chalkItem.getTintColor(itemStack, 0);
            particleData = new DustParticleOptions(color, 2f);
        }

        original.call(instance, particleData, x, y, z, xd, yd, zd);
    }
}
