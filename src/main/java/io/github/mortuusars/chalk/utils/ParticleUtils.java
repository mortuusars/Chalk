package io.github.mortuusars.chalk.utils;

import com.mojang.math.Vector3f;
import io.github.mortuusars.chalk.render.ChalkColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import java.util.Random;

public class ParticleUtils {

    /**
     * Spawns a particle with slight random offset to each. Includes velocity.
     */
    public static void spawnParticle(Level level, ParticleOptions particleType, Vector3f position, Vector3f velocity, int count){
        if (!level.isClientSide() || count < 1)
            return;

        for (int i=0; i < count; i++ ){
            level.addParticle(particleType,
                    position.x() + ((level.getRandom().nextFloat() - 0.5f) * 0.3),
                    position.y() + ((level.getRandom().nextFloat() - 0.5f) * 0.3),
                    position.z() + ((level.getRandom().nextFloat() - 0.5f) * 0.3),
                    velocity.x(),
                    velocity.y(),
                    velocity.z());
        }
    }

    /**
     * Spawns a particle with slight random offset to each.
     */
    public static void spawnParticle(Level level, ParticleOptions particleType, Vector3f position, int count){
        spawnParticle(level, particleType, position, new Vector3f(0f, 0f, 0f), count);
    }

    /**
     * Spawns a color dust particles at the blockPos, close to the specified face.
     */
    public static void spawnColorDustParticles(DyeColor color, Level level, BlockPos pos, Direction face){
        int colorValue = ChalkColors.fromDyeColor(color);
        float R = (colorValue & 0x00FF0000) >> 16;
        float G = (colorValue & 0x0000FF00) >> 8;
        float B = (colorValue & 0x000000FF);

        ParticleUtils.spawnParticle(level, new DustParticleOptions(new Vector3f(R / 255, G / 255, B / 255), 2f),
                PositionUtils.blockCenterOffsetToFace(pos, face, 0.25f), 1);
    }
}
