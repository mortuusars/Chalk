package io.github.mortuusars.chalk.utils;

import com.mojang.math.Vector3f;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;

import java.util.Random;

public class ParticleUtils {

    /**
     * Spawns a particle with slight random offset to each. Includes velocity.
     */
    public static void spawnParticle(Level world, ParticleOptions particleType, Vector3f position, Vector3f velocity, int count){
        if (!world.isClientSide() || count < 1)
            return;

        Random random = new Random();

        for (int i=0; i < count; i++ ){
            world.addParticle(particleType,
                    position.x() + ((random.nextFloat() - 0.5f) * 0.3),
                    position.y() + ((random.nextFloat() - 0.5f) * 0.3),
                    position.z() + ((random.nextFloat() - 0.5f) * 0.3),
                    velocity.x(),
                    velocity.y(),
                    velocity.z());
        }
    }

    /**
     * Spawns a particle with slight random offset to each.
     */
    public static void spawnParticle(Level world, ParticleOptions particleType, Vector3f position, int count){
        spawnParticle(world, particleType, position, new Vector3f(0f, 0f, 0f), count);
    }
}
