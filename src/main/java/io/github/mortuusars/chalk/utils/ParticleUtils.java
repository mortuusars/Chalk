package io.github.mortuusars.chalk.utils;

import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

import java.util.Random;

public class ParticleUtils {

    public static void spawnParticle(World world, IParticleData particleType, Vector3f position, Vector3f velocity, int count){
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

    public static void spawnParticle(World world, IParticleData particleType, Vector3f position, int count){
        spawnParticle(world, particleType, position, new Vector3f(0f, 0f, 0f), count);
    }
}
