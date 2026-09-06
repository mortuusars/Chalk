package io.github.mortuusars.chalk.client;

import io.github.mortuusars.chalk.network.packet.serverbound.DestroyMarkServerboundPacket;
import io.github.mortuusars.chalk.world.block.MarkBlock;
import io.github.mortuusars.chalk.world.chalk.DrawnMark;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Objects;

public class ClientHelper {
    public static boolean handleCreativeStartDestroyBlock(BlockPos pos, Direction face, GameType localPlayerMode) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = Objects.requireNonNull(minecraft.level);
        LocalPlayer player = Objects.requireNonNull(minecraft.player);

        if (minecraft.hitResult instanceof BlockHitResult hitResult
              && hitResult.getType() != HitResult.Type.MISS
              && !player.blockActionRestricted(level, pos, localPlayerMode)
              && level.getWorldBorder().isWithinBounds(pos)
              && level.getBlockState(pos).getBlock() instanceof MarkBlock) {
            if (MarkBlock.getMarkAt(level, hitResult.getLocation()) instanceof DrawnMark mark) {
                new DestroyMarkServerboundPacket(pos, mark.facing()).sendToServer();
            }

            return true;
        }

        return false;
    }

    public static void attackMarkBlock(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = Objects.requireNonNull(minecraft.level);

        if (minecraft.hitResult instanceof BlockHitResult hitResult
              && hitResult.getType() != HitResult.Type.MISS
              && level.getWorldBorder().isWithinBounds(pos)
              && level.getBlockState(pos).getBlock() instanceof MarkBlock
              && MarkBlock.getMarkAt(level, hitResult.getLocation()) instanceof DrawnMark mark) {
            new DestroyMarkServerboundPacket(pos, mark.facing()).sendToServer();
        }
    }
}
